package uk.ac.kcl.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;
import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.mutators.Mutant;
import uk.ac.kcl.mutators.StringMutatorService;
import uk.ac.kcl.service.ESRestService;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by rich on 13/05/16.
 */
@Ignore
@Service
@ComponentScan({"uk.ac.kcl"})
public class TestUtils  {
    static Random random = new Random();
    static long today = System.currentTimeMillis();
    final static Logger logger = Logger.getLogger(TestUtils.class);
    final static String[] biolarkText ={"Disproportionate dwarfism.",
            "Shortening of the proximal limbs (called rhizomelic shortening).",
            "Short fingers and toes with trident hands.",
            "Large head with prominent forehead frontal bossing.",
            "Small midface with a flattened nasal bridge.",
            "Spinal kyphosis (convex curvature) or lordosis (concave curvature).",
            "Varus (bowleg) or valgus (knock knee) deformities.",
            "Frequently have ear infections (due to Eustachian tube blockages), sleep apnea " +
                    "(which can be central or obstructive), and hydrocephalus."};
    @Autowired
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource;

    @Autowired
    @Qualifier("targetDataSource")
    public DataSource targetDataSource;

    @Autowired
    StringMutatorService stringMutatorService;

    @Autowired(required = false)
    @Qualifier("esRestService")
    private ESRestService esRestService;

    @Autowired
    Environment env;

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
                + ", sometext"
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
                logger.error(ex);
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
                + ", sometext"
                + ") VALUES (?,?,?,?,?,?)";
        for (int ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", ii, new Timestamp(today), bytes);
            today = TestUtils.nextDay();
        }
    }

    public void insertTestBinariesForPdfbox (String tableName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 55;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("pdfbox/testdocs/pdfWithForm.pdf"));
        } catch (IOException ex) {
            logger.fatal(ex);
        }

        String sql = "INSERT INTO  " + tableName
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", sometext"
                + ") VALUES (?,?,?,?,?,?)";
        for (int ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", ii, new Timestamp(today), bytes);
            today = TestUtils.nextDay();
        }
    }

    public void insertDataIntoBasicTable( String tableName,boolean includeText, int startId,int endId,boolean sameDay){
        if(!sameDay)today = TestUtils.nextDay();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        String sql = "INSERT INTO  " + tableName
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", someText"
                + ", anotherTime"
                + ") VALUES (?,?,?,?,?,?,?)";


        String biolarkTs = "";
        for(int i=0;i<biolarkText.length;i++){
            biolarkTs = biolarkTs +" " +biolarkText[i];
        }
        for (long i = startId; i <= endId; i++) {

            if(includeText) {
                jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName",
                        "fictionalPrimaryKeyFieldName", i, new Timestamp(today), biolarkTs, new Timestamp(today));
            }else{
                jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName",
                        "fictionalPrimaryKeyFieldName", i, new Timestamp(today), null, new Timestamp(today));
            }
        }
    }



    public void insertDataIntoDocmanTable( String tableName){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        String sql = "INSERT INTO  " + tableName
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", someText"
                + ", path"
                + ") VALUES (?,?,?,?,?,?,?)";

            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName",
                        "fictionalPrimaryKeyFieldName", 1, new Timestamp(today), "patient 1 document",
                        "tika/testdocs/pat_id_1.doc");
            today = TestUtils.nextDay();
            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName",
                    "fictionalPrimaryKeyFieldName", 2, new Timestamp(today), "patient 2 document",
                    "tika/testdocs/pat_id_2.doc");
    }



    public List<Mutant> insertTestDataForDeidentification(String tableName1, String tableName2,
                                                          int mutationLevel,boolean useBigList){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);

        File idFile;
        if(useBigList) {
            idFile = new File(getClass().getClassLoader().getResource("identifiers.csv").getFile());
        }else{
            idFile = new File(getClass().getClassLoader().getResource("identifiers_small.csv").getFile());
        }

        List<CSVRecord> records = null;
        try {
            records = CSVParser.parse(idFile, Charset.defaultCharset(), CSVFormat.DEFAULT).getRecords();
        } catch (IOException e) {
            logger.error(e);
        }

        String sql1 = "INSERT INTO  " + tableName1
                + "( primaryKeyFieldValue "
                + ",   NAME"
                + ", ADDRESS"
                + ", POSTCODE"
                + ", DATE_OF_BIRTH"
                + ") VALUES (?,?,?,?,?)";

        String sql2 = "INSERT INTO  " + tableName2
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", someText"
                + ", anotherTime"
                + ") VALUES (?,?,?,?,?,?,?)";

        Iterator<CSVRecord> it = records.iterator();
        it.next();

        List<Mutant> mutants = new ArrayList<>();
        while(it.hasNext()){
            CSVRecord r = it.next();


            String[] stringToMutate = convertCsvRecordToStringArrayAddressOnly(r);
            Mutant mutant = stringMutatorService.generateMutantDocument(stringToMutate, mutationLevel);
            mutant.setDocumentid(Long.valueOf(r.get(0)));
            mutants.add(mutant);
            jdbcTemplate.update(sql1, Long.valueOf(r.get(0)),r.get(1),r.get(2),r.get(3), new Timestamp(today));
            jdbcTemplate.update(sql2, "fictionalColumnFieldName", "fictionalTableName",
                    "fictionalPrimaryKeyFieldName", Long.valueOf(r.get(0)),
                    new Timestamp(today),mutant.getFinalText(),
                    new Timestamp(today));
            today = TestUtils.nextDay();
        }
        return mutants;
    }


    public List<Mutant> insertTestDataForFullPipeline(String tableName1, String tableName2, int mutationLevel){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);

        File idFile = new File(getClass().getClassLoader().getResource("identifiers_small.csv").getFile());

        List<CSVRecord> records = null;
        try {
            records = CSVParser.parse(idFile, Charset.defaultCharset(), CSVFormat.DEFAULT).getRecords();
        } catch (IOException e) {
            logger.error(e);
        }


        String sql1 = "INSERT INTO  " + tableName1
                + "( primaryKeyFieldValue "
                + ",   NAME"
                + ", ADDRESS"
                + ", POSTCODE"
                + ", DATE_OF_BIRTH"
                + ") VALUES (?,?,?,?,?)";

        String sql2 = "INSERT INTO  " + tableName2
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", sometext"
                + ") VALUES (?,?,?,?,?,?)";

        Iterator<CSVRecord> it = records.listIterator();
        it.next();
        List<Mutant> mutants = new ArrayList<>();
        while (it.hasNext()) {
            CSVRecord r = it.next();
            long id = Long.valueOf(r.get(0));
            Mutant massiveDoc = stringMutatorService.generateMutantDocument(convertCsvRecordToStringArray(r)
                    ,biolarkText, mutationLevel);
            mutants.add(massiveDoc);
            jdbcTemplate.update(sql2, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", id,
                    new Timestamp(today), convertObjectToByteArray(generateDocxDocument(multiplyDocSize(massiveDoc.getFinalText(),0))));
            today = TestUtils.nextDay();
            jdbcTemplate.update(sql1, id, r.get(1), r.get(2), r.get(3), new Timestamp(today));
        }
        return mutants;
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



    public void insertFreshDataIntoBasicTableAfterDelay(String tablename,long delay,int start,int end, boolean sameDay) {
        try {
            Thread.sleep(delay);
            esRestService.init();
            System.out.println("********************* INSERTING FRESH DATA*******************");
            insertDataIntoBasicTable(tablename,true,start,end,sameDay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertJsonsIntoInputTable(String s) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);

        File jsonFile = new File(getClass().getClassLoader().getResource("jsonExamples.txt").getFile());


        String sql = "INSERT INTO  " + s
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", someText )"
                + " VALUES (?,?,?,?,?,?)";


        //Construct BufferedReader from InputStreamReader
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile)))){
            String line = null;
            long l = 0;
            while ((line = br.readLine()) != null) {
                jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName",
                        "fictionalPrimaryKeyFieldName", l, new Timestamp(today),line);
                today = TestUtils.nextDay();
                l++;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XWPFDocument generateDocxDocument(String content)  {
        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph p1 = doc.createParagraph();
        p1.setAlignment(ParagraphAlignment.CENTER);
        p1.setBorderBottom(Borders.DOUBLE);
        p1.setBorderTop(Borders.DOUBLE);

        p1.setBorderRight(Borders.DOUBLE);
        p1.setBorderLeft(Borders.DOUBLE);
        p1.setBorderBetween(Borders.SINGLE);

        p1.setVerticalAlignment(TextAlignment.TOP);

        XWPFRun r1 = p1.createRun();
        r1.setBold(true);
        r1.setText(content);
        r1.setBold(true);
        r1.setFontFamily("Courier");
        r1.setUnderline(UnderlinePatterns.DOT_DOT_DASH);
        r1.setTextPosition(100);
        return doc;
    }

    private byte[] convertObjectToByteArray(XWPFDocument o){


        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){

            o.write(b);
            return b.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] convertCsvRecordToStringArray(CSVRecord r){
        ArrayList<String> list = new ArrayList<>();
        list.add(r.get(1));
        list.add(r.get(2));
        list.add(r.get(3));
        String[] arr = new String[list.size()];
        arr = list.toArray(arr);
        return arr;
    }

    public static String[] convertCsvRecordToStringArrayAddressOnly(CSVRecord r){
        ArrayList<String> list = new ArrayList<>();
        list.add(r.get(2) + r.get(3));
        String[] arr = new String[list.size()];
        arr = list.toArray(arr);
        return arr;
    }


    public void deleteESTestIndexAndSetUpMapping(){

        try {
            esRestService.getRestClient().performRequest(
                    "DELETE",
                    "/"+env.getProperty("elasticsearch.index.name") ,
                    Collections.<String, String>emptyMap()
                    );
        } catch (IOException e) {
            //may not exist.
//            throw new RuntimeException("Delete failed:", e);
        }
        setUpESMapping();
    }
    private void setUpESMapping(){
        HttpEntity entity = new NStringEntity("{" +
                "  \"mappings\": {" +
                "    \""+env.getProperty("elasticsearch.type")+"\": {" +
                "      \"properties\": {" +
                "        \""+env.getProperty("webservice.name")+"\": {" +
                "          \"type\": \"nested\" " +
                "        }," +
                "        \""+env.getProperty("gateFieldName")+"\": {" +
                "          \"type\": \"nested\" " +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}", ContentType.APPLICATION_JSON);

        try {
            esRestService.getRestClient().performRequest(
                    "PUT",
                    "/"+env.getProperty("elasticsearch.index.name") ,
                    Collections.<String, String>emptyMap(),
                    entity
            );
        } catch (IOException e) {
            throw new RuntimeException("Delete failed:", e);
        }
    }


    public int countOutputDocsInES(){
        try {
            esRestService.init();
           Response response = esRestService.getRestClient().performRequest(
                    "GET",
                    "/"+env.getProperty("elasticsearch.index.name")+"/"
                            +env.getProperty("elasticsearch.type")+"/_count",
                    Collections.<String, String>emptyMap()
            );
            HttpEntity ent = response.getEntity();
            JSONObject json = new JSONObject(EntityUtils.toString(ent));
            return  json.getInt("count");
        } catch (IOException e) {
            throw new RuntimeException("GET request failed:", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    public String getStringInEsDoc(String id){
        try {
            Response response = esRestService.getRestClient().performRequest(
                    "GET",
                    "/"+env.getProperty("elasticsearch.index.name")+"/"
                            +env.getProperty("elasticsearch.type")+"/"+id,
                    Collections.<String, String>emptyMap()
            );
            HttpEntity ent = response.getEntity();
            JsonObject json = new JsonParser().parse(EntityUtils.toString(ent)).getAsJsonObject();
            return  json.toString();
        } catch (IOException e) {
            throw new RuntimeException("GET request failed:", e);
        }
    }



    private String multiplyDocSize(String doc, int factor){
        String newDoc = new String(doc);
        for(int i=0;i<factor;i++) {
            newDoc = newDoc + doc;
        }
        return newDoc;
    }

}
