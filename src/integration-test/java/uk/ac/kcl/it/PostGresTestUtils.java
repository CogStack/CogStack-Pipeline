package uk.ac.kcl.it;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
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


import uk.ac.kcl.batch.JobConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
        "classpath:postgres_db.properties",
        "classpath:elasticsearch.properties",
        "classpath:jobAndStep.properties"})
@Configuration
@Import(JobConfiguration.class)
public class PostGresTestUtils {

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


    public void initPostgresTikaTable() {
////        for postgres
        sourceTemplate.execute("DROP TABLE IF EXISTS tblInputDocs");
        sourceTemplate.execute("CREATE TABLE tblInputDocs"
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue integer "
                + ", updateTime TIMESTAMP "
                + ", input bytea )");

        targetTemplate.execute("DROP TABLE IF EXISTS tblOutputDocs");
        targetTemplate.execute("CREATE TABLE tblOutputDocs "
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue integer "
                + ", updateTime TIMESTAMP "
                + ", output text )");
    }

    public void initTextualPostgresGateTable() {
////        for postgres
        sourceTemplate.execute("DROP TABLE IF EXISTS tblInputDocs");
        sourceTemplate.execute("CREATE TABLE tblInputDocs"
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue integer "
                + ", updateTime Date "
                + ", input text )");

        targetTemplate.execute("DROP TABLE IF EXISTS tblOutputDocs");
        targetTemplate.execute("CREATE TABLE tblOutputDocs "
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue integer "
                + ", updateTime Date "
                + ", output text )");
    }


    public void createBasicInputTable(){
        sourceTemplate.execute("DROP TABLE IF EXISTS tblInputDocs");
        sourceTemplate.execute("CREATE TABLE tblInputDocs"
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue integer "
                + ", updateTime Date )");

    }

    public void createBasicOutputTable(){
        targetTemplate.execute("DROP TABLE IF EXISTS tblOutputDocs");
        targetTemplate.execute("CREATE TABLE tblOutputDocs "
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue integer "
                + ", updateTime Date )");


    }

    public void initPostgresMultiLineTextTable(){
        createBasicInputTable();
        sourceTemplate.execute("DROP TABLE IF EXISTS tblDocLines");
        sourceTemplate.execute("CREATE TABLE tblDocLines"
                + "( ID  SERIAL PRIMARY KEY"
                + ", primaryKeyFieldValue integer "
                + ", updateTime Date "
                + ", LINE_ID integer "
                + ", LINE_TEXT text )"
        );


        targetTemplate.execute("DROP TABLE IF EXISTS tblOutputDocs");
        targetTemplate.execute("CREATE TABLE tblOutputDocs "
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue integer "
                + ", updateTime Date "
                + ", LINE_TEXT_CONCAT text )");
    }



    public void initPostGresJobRepository(){
        dropTablesResource = new ClassPathResource("org/springframework/batch/core/schema-drop-postgresql.sql");
        makeTablesResource = new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql");
        rdp.addScript(dropTablesResource);
        rdp.addScript(makeTablesResource);
        rdp.execute(jdbcTargetDocumentFinder);
    }

    public void insertTestXHTMLForGate( boolean includeGateBreaker) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 100;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("xhtml_test"));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PostGresIntegrationTestsGATE.class.getName()).log(Level.SEVERE, null, ex);
        }
        String xhtmlString = new String(bytes, StandardCharsets.UTF_8);

        String sql = "INSERT INTO tblInputDocs "
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", input"
                + ") VALUES (?,?,?,?,?,?)";
        for (int ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName","fictionalTableName","fictionalPrimaryKeyFieldName", ii,new Date((ii*1000*60*60*24)),  xhtmlString);

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
        int docCount = 100;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("tika/testdocs/docexample.doc"));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PostGresIntegrationTestsTika.class.getName()).log(Level.SEVERE, null, ex);
        }

        String sql = "INSERT INTO tblInputDocs "
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", input"
                + ") VALUES (?,?,?,?,?,?)";
        for (int ii = 0; ii < docCount; ii++) {
            jdbcTemplate.update(sql, "fictionalColumnFieldName", "fictionalTableName", "fictionalPrimaryKeyFieldName", ii, new Date((ii*1000*60*60*24)), bytes);
        }
    }

    public void insertDataIntoBasicTable(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        int docCount = 34;
        int lineCountIncrementer = 1;
        String sql = "INSERT INTO tblInputDocs "
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
        String sql = "INSERT INTO tblDocLines "
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
