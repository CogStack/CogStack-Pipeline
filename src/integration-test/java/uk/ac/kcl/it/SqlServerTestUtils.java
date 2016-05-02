package uk.ac.kcl.it;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptStatementFailedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.kcl.batch.JobConfiguration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.logging.Level;

/*
 * Copyright 2016 King's College London, Richard Jackson <richgjackson@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource({
        "classpath:postgres_test_config_gate.properties",
        "classpath:jms.properties",
        "classpath:gate.properties",
        "classpath:concurrency.properties",
        "classpath:sql_server_db.properties",
        "classpath:elasticsearch.properties",
        "classpath:jobAndStep.properties"})
@Configuration
@Import(JobConfiguration.class)
public class SqlServerTestUtils {

    final static Logger logger = Logger.getLogger(PostGresIntegrationTestsGATE.class);

    @Autowired
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource;

    @Autowired
    @Qualifier("targetDataSource")
    public DataSource jdbcTargetDocumentFinder;

    private JdbcTemplate sourceTemplate;
    private JdbcTemplate targetTemplate;
    private ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
    private Resource dropTablesResource;
    private Resource makeTablesResource;


    @PostConstruct
    public void init(){
        this.sourceTemplate = new JdbcTemplate(sourceDataSource);
        this.targetTemplate = new JdbcTemplate(jdbcTargetDocumentFinder);
    }


    public void initTikaTable() {
////        for postgres
        sourceTemplate.execute("IF OBJECT_ID('dbo.tblInputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblInputDocs");
        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", input VARBINARY(max) )");

        sourceTemplate.execute("IF OBJECT_ID('dbo.tblOutputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", output text )");
    }

    public void initTextualPostgresGateTable() {
////        for postgres
        sourceTemplate.execute("DROP TABLE IF EXISTS dbo.tblInputDocs");
        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", input text )");

        targetTemplate.execute("DROP TABLE IF EXISTS dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", output text )");
    }


    public void createBasicInputTable(){
        sourceTemplate.execute("DROP TABLE IF EXISTS dbo.tblInputDocs");
        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME )");

    }

    public void createBasicOutputTable(){
        targetTemplate.execute("DROP TABLE IF EXISTS dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME )");


    }

    public void initPostgresMultiLineTextTable(){
        createBasicInputTable();
        sourceTemplate.execute("DROP TABLE IF EXISTS dbo.tblDocLines");
        sourceTemplate.execute("CREATE TABLE dbo.tblDocLines"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY,"
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", LINE_ID BIGINT "
                + ", LINE_TEXT VARCHAR(MAX) )"
        );


        targetTemplate.execute("DROP TABLE IF EXISTS dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName varchar(max) "
                + ", srcTableName varchar(max) "
                + ", primaryKeyFieldName varchar(max) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DATETIME "
                + ", LINE_TEXT_CONCAT varchar(max) )");
    }



    public void initJobRepository(){
        dropTablesResource = new ClassPathResource("org/springframework/batch/core/schema-drop-sqlserver.sql");
        makeTablesResource = new ClassPathResource("org/springframework/batch/core/schema-sqlserver.sql");
//        try {
            rdp.addScript(dropTablesResource);
//            rdp.execute(jdbcTargetDocumentFinder);
//        }catch(ScriptStatementFailedException ex){
//            logger.info("Job repository not deleted. It might not exist");
//        }
        rdp.addScript(makeTablesResource);

        rdp.setIgnoreFailedDrops(true);
        rdp.setContinueOnError(true);
        rdp.execute(jdbcTargetDocumentFinder);
    }

    public void insertTestXHTMLForGate( boolean includeGateBreaker) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 100000;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("xhtml_test"));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PostGresIntegrationTestsGATE.class.getName()).log(Level.SEVERE, null, ex);
        }
        String xhtmlString = new String(bytes, StandardCharsets.UTF_8);

        String sql = "INSERT INTO dbo.tblInputDocs "
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", input"
                + ") VALUES (?,?,?,?,?,?)";
        for (long ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName","fictionalTableName","fictionalPrimaryKeyFieldName", ii,new Date((ii*1000L*60L*60L*24L)),  xhtmlString);

        }
        //see what happens with a really long document...
        if (includeGateBreaker) {
            try {
                bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("gate_breaker.txt"));
                xhtmlString = new String(bytes, StandardCharsets.UTF_8);
                jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", docCount, null, xhtmlString);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(PostGresIntegrationTestsGATE.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void insertTestBinariesForTika() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 100000;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("tika/testdocs/docexample.doc"));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PostGresIntegrationTestsTika.class.getName()).log(Level.SEVERE, null, ex);
        }

        String sql = "INSERT INTO dbo.tblInputDocs "
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", input"
                + ") VALUES (?,?,?,?,?,?)";
        for (long ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", ii, new Date((ii*1000L*60L*60L*24L)), bytes);
        }
    }

    public void insertDataIntoBasicTable(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 34;
        int lineCountIncrementer = 1;
        String sql = "INSERT INTO dbo.tblInputDocs "
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ") VALUES (?,?,?,?,?)";
        for (int i = 0; i <= docCount; i++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName","fictionalTableName","fictionalPrimaryKeyFieldName",i,new Date((i*1000*60*60*24)));
        }
    }


    public void insertTestLinesForDBLineFixer(){
        insertDataIntoBasicTable();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int lineCountIncrementer = 1;
        int docCount = 100;
        String sql = "INSERT INTO dbo.tblDocLines "
                + "( primaryKeyFieldValue"
                + ", updateTime"
                + ", LINE_ID"
                + ", LINE_TEXT"
                + ") VALUES (?,?,?,?)";
        for (int i = 0; i <= docCount; i++) {
            for(int j = 0;j < lineCountIncrementer; j++){
                String text = "This is DOC_ID:" + i + " and LINE_ID:" + j ;
                jdbcTemplate.update(sql,i,new Date((i*1000*60*60*24)),j,text);
            }
            lineCountIncrementer++;
            if(lineCountIncrementer % 50 == 0){
                lineCountIncrementer = 0;
            }
        }

    }
}
