package uk.ac.kcl.it;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.kcl.batch.JobConfiguration;

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

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource({
        "classpath:sql_server_db.properties"})
@Configuration
@Import({JobConfiguration.class,TestUtils.class})
@Ignore
public class SqlServerTestUtils implements DbmsTestUtils {

    final static Logger logger = Logger.getLogger(SqlServerTestUtils.class);

    @Autowired
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource;

    @Autowired
    @Qualifier("targetDataSource")
    public DataSource targetDataSource;

    @Autowired
    @Qualifier("jobRepositoryDataSource")
    public DataSource jobRepositoryDataSource;

    private JdbcTemplate sourceTemplate;
    private JdbcTemplate targetTemplate;
    private ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();


    @Override
    @PostConstruct
    public void init(){
        this.sourceTemplate = new JdbcTemplate(sourceDataSource);
        this.targetTemplate = new JdbcTemplate(targetDataSource);
        JdbcTemplate jobRepoTemplate = new JdbcTemplate(jobRepositoryDataSource);
    }


    @Override
    public void createTikaTable() {
////        for postgres
        sourceTemplate.execute("IF OBJECT_ID('dbo.tblInputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblInputDocs");
        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", binaryContent VARBINARY(max))");

        targetTemplate.execute("IF OBJECT_ID('dbo.tblOutputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", output text )");

    }

    @Override
    public void createTextualGateTable() {
        sourceTemplate.execute("IF OBJECT_ID('dbo.tblInputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblInputDocs");
        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", input text )");

        targetTemplate.execute("IF OBJECT_ID('dbo.tblOutputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", output text )");
    }


    @Override
    public void createBasicInputTable(){
        sourceTemplate.execute("IF OBJECT_ID('dbo.tblInputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblInputDocs");
        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", someText VARCHAR (MAX)"
                + ", anotherTime DateTIME )");


    }



    @Override
    public void createBasicOutputTable(){
        targetTemplate.execute("IF OBJECT_ID('dbo.tblOutputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName VARCHAR(MAX) "
                + ", srcTableName VARCHAR(MAX) "
                + ", primaryKeyFieldName VARCHAR(MAX) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", output VARCHAR(MAX) "
                + ", anotherTime DateTIME  )" );


    }

    @Override
    public void createMultiLineTextTable(){
        createBasicInputTable();
        sourceTemplate.execute("IF OBJECT_ID('dbo.tblDocLines', 'U') IS NOT NULL DROP TABLE  dbo.tblDocLines");
        sourceTemplate.execute("CREATE TABLE dbo.tblDocLines"
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DateTIME "
                + ", LINE_ID BIGINT "
                + ", LINE_TEXT VARCHAR(MAX) )"
        );
        targetTemplate.execute("IF OBJECT_ID('dbo.tblOutputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblOutputDocs");
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", srcColumnFieldName varchar(max) "
                + ", srcTableName varchar(max) "
                + ", primaryKeyFieldName varchar(max) "
                + ", primaryKeyFieldValue BIGINT "
                + ", updateTime DATETIME "
                + ", LINE_TEXT_CONCAT varchar(max) )");
    }



    @Override
    public void createJobRepository(){
        Resource dropTablesResource = new ClassPathResource("org/springframework/batch/core/schema-drop-sqlserver.sql");
        Resource makeTablesResource = new ClassPathResource("org/springframework/batch/core/schema-sqlserver.sql");
        rdp.addScript(dropTablesResource);
        rdp.addScript(makeTablesResource);
        rdp.setIgnoreFailedDrops(true);
        rdp.setContinueOnError(true);
        rdp.execute(jobRepositoryDataSource);
    }

    @Override
    public void createDeIdInputTable(){
        createBasicInputTable();

        sourceTemplate.execute("IF OBJECT_ID('dbo.vwidentifiers', 'U') IS NOT NULL DROP TABLE  dbo.vwidentifiers");
        sourceTemplate.execute("IF OBJECT_ID('dbo.tblIdentifiers', 'U') IS NOT NULL DROP TABLE  dbo.tblIdentifiers");



        sourceTemplate.execute("CREATE TABLE dbo.tblIdentifiers "
                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
                + ", primaryKeyFieldValue BIGINT "
                + ", NAME VARCHAR(MAX) "
                + ", ADDRESS VARCHAR(MAX) "
                + ", POSTCODE VARCHAR(MAX) "
                + ", DATE_OF_BIRTH DATETIME )");
        sourceTemplate.execute("create view dbo.vwIdentifiers AS\n" +
                "  select primarykeyfieldvalue, address as identifier from dbo.tblidentifiers\n" +
                "  UNION\n" +
                "  select primarykeyfieldvalue, name  as identifier from dbo.tblidentifiers\n" +
                "  UNION\n" +
                "  select primarykeyfieldvalue, postcode as identifier  from dbo.tblidentifiers");

//        sourceTemplate.execute("IF OBJECT_ID('dbo.tblInputDocs', 'U') IS NOT NULL DROP TABLE  dbo.tblInputDocs");
//        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
//                + "( ID  BIGINT IDENTITY(1,1) PRIMARY KEY"
//                + ", srcColumnFieldName VARCHAR(MAX) "
//                + ", srcTableName VARCHAR(MAX) "
//                + ", primaryKeyFieldName VARCHAR(MAX) "
//                + ", primaryKeyFieldValue BIGINT "
//                + ", updateTime DateTIME "
//                + ", someText VARCHAR (MAX)"
//                + ", anotherTime DateTIME )");
    }
}
