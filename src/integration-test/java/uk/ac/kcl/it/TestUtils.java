package uk.ac.kcl.it;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Random;
import java.util.logging.Level;

/**
 * Created by rich on 13/05/16.
 */
@Service
public class TestUtils  {
    static Random random = new Random();
    static long today = System.currentTimeMillis();
    final static Logger logger = Logger.getLogger(TestUtils.class);
    @Autowired
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource;

    @Autowired
    @Qualifier("targetDataSource")
    public DataSource jdbcTargetDocumentFinder;


    public static long nextDay() {

        // error checking and 2^x checking removed for simplicity.
        long bits, val;
        do {
            bits = (random.nextLong() << 1L) >>> 1L;
            val = bits % 20000L;
        } while (bits-val+(20000L-1L) < 0L);
        today = today + 86400000L +val;
        return today;
    }

    public void insertTestXHTMLForGate( String tableName, boolean includeGateBreaker) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 100;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("xhtml_test"));
        } catch (IOException ex) {
            logger.fatal(ex);
        }
        String xhtmlString = new String(bytes, StandardCharsets.UTF_8);

        String sql = "INSERT INTO  " + tableName
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", input"
                + ") VALUES (?,?,?,?,?,?)";
        for (long ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", ii, new Timestamp(today), xhtmlString);
            today = TestUtils.nextDay();

        }
        //see what happens with a really long document...
        if (includeGateBreaker) {
            try {
                bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("gate_breaker.txt"));
                xhtmlString = new String(bytes, StandardCharsets.UTF_8);
                jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", docCount, null, xhtmlString);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(PostGresIntegrationTestsGATEPKPartitionWithoutScheduling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void insertTestBinariesForTika( String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 1000;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("tika/testdocs/docexample.doc"));
        } catch (IOException ex) {
            logger.fatal(ex);
        }

        String sql = "INSERT INTO  " + tableName
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", binaryContent"
                + ") VALUES (?,?,?,?,?,?)";
        for (int ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", ii, new Timestamp(today), bytes);
            today = TestUtils.nextDay();
        }
    }

    public void insertDataIntoBasicTable( String tableName){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 75;
        int lineCountIncrementer = 1;
        String sql = "INSERT INTO  " + tableName
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", someText"
                + ", anotherTime"
                + ") VALUES (?,?,?,?,?,?,?)";


        String string1 = "Disproportionate dwarfism.\n" +
                "Shortening of the proximal limbs (called rhizomelic shortening).\n" +
                "Short fingers and toes with trident hands.\n" +
                "Large head with prominent forehead frontal bossing.\n" +
                "Small midface with a flattened nasal bridge.\n" +
                "Spinal kyphosis (convex curvature) or lordosis (concave curvature).\n" +
                "Varus (bowleg) or valgus (knock knee) deformities.\n" +
                "Frequently have ear infections (due to Eustachian tube blockages), sleep apnea (which can be central or obstructive), and hydrocephalus.";
        for (long i = 1; i <= docCount; i++) {

                jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName",
                        "fictionalPrimaryKeyFieldName", i, new Timestamp(today),string1, new Timestamp(today));
//            if (i==0) {
//                //test for massive string in ES
//                jdbcTemplate.update(sql, RandomString.nextString(50), "fictionalTableName",
//                        "fictionalPrimaryKeyFieldName", i, new Timestamp(today),string1,new Timestamp(today));
//            }else{
//                jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName",
//                        "fictionalPrimaryKeyFieldName", i, new Timestamp(today),string1, new Timestamp(today));
//            }
            today = TestUtils.nextDay();
        }
    }


    public void insertTestLinesForDBLineFixer(String tableName){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int lineCountIncrementer = 1;
        int docCount = 100;
        String sql = "INSERT INTO  " + tableName
                + "( primaryKeyFieldValue"
                + ", updateTime"
                + ", LINE_ID"
                + ", LINE_TEXT"
                + ") VALUES (?,?,?,?)";
        for (int i = 0; i <= docCount; i++) {
            for(int j = 0;j < lineCountIncrementer; j++){
                String text = "This is DOC_ID:" + i + " and LINE_ID:" + j ;
                jdbcTemplate.update(sql,i,new Timestamp(today),j,text);
                today = TestUtils.nextDay();
            }
            lineCountIncrementer++;
            if(lineCountIncrementer % 50 == 0){
                lineCountIncrementer = 0;
            }
        }
    }

    public void insertFreshDataIntoBasicTableAfterDelay(String tablename,long delay) {
        try {
            Thread.sleep(delay);
            logger.info("********************* INSERTING FRESH DATA*******************");
            insertDataIntoBasicTable(tablename);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
