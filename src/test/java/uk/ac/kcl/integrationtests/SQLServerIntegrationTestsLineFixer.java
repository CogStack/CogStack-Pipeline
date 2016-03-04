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
package uk.ac.kcl.integrationtests;

import uk.ac.kcl.batch.JobConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.batch.BatchConfigurer;
import uk.ac.kcl.batch.io.DbLineFixerIOConfiguration;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:sqlserver_test_config_line_fixer.properties")
//@ContextConfiguration(locations = {"classpath:testApplicationContext.xml"})
@ContextConfiguration(classes = {
    JobConfiguration.class,
    BatchConfigurer.class,
    DbLineFixerIOConfiguration.class
    },
        loader = AnnotationConfigContextLoader.class)
public class SQLServerIntegrationTestsLineFixer  {

    final static Logger logger = Logger.getLogger(SQLServerIntegrationTestsLineFixer.class);

    @Autowired
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource;

    @Autowired
    @Qualifier("targetDataSource")
    public DataSource jdbcTargetDocumentFinder;

    private  Server server1;
    private  Server server2;
    private JdbcTemplate sourceTemplate;
    private JdbcTemplate targetTemplate;
    private ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
    private Resource dropTablesResource;
    private Resource makeTablesResource;


    
    @Before
    public void initTemplates() {
        sourceTemplate = new JdbcTemplate(sourceDataSource);
        targetTemplate = new JdbcTemplate(jdbcTargetDocumentFinder);
    }

    @After
    public void dropDb() {
        //sourceTemplate.execute("DROP TABLE tblInputDocs");
        //targetTemplate.execute("DROP TABLE tblOutputDocs");
    }


    @Autowired
    JobOperator jobOperator;

    
    @Ignore
    @Test
    public void sqlserverDBLineFixerPipelineTest() {
        
        initSqlServerJobRepository();
        initSqlServerMultiLineTextTable();
        insertTestLinesForDBLineFixer(sourceDataSource);

        try {
            jobOperator.startNextInstance("dbLineFixerJob");
        } catch (NoSuchJobException | JobParametersNotFoundException | JobRestartException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | UnexpectedJobExecutionException | JobParametersInvalidException ex) {
            java.util.logging.Logger.getLogger(SQLServerIntegrationTestsLineFixer.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    private void initSqlServerJobRepository(){
        dropTablesResource = new ClassPathResource("org/springframework/batch/core/schema-drop-sqlserver.sql");
        makeTablesResource = new ClassPathResource("org/springframework/batch/core/schema-sqlserver.sql");
        rdp.addScript(dropTablesResource);
        rdp.addScript(makeTablesResource);
        rdp.execute(jdbcTargetDocumentFinder);     
    }    

    
    private void initSqlServerMultiLineTextTable(){
        sourceTemplate.execute("IF OBJECT_ID('dbo.tblInputDocs', 'U') IS NOT NULL DROP TABLE dbo.tblInputDocs");        
        sourceTemplate.execute("CREATE TABLE dbo.tblInputDocs"
                + "( ID  bigint IDENTITY(1,1) PRIMARY KEY"
                + ", DOC_ID bigint "
                + ", LINE_ID bigint "
                + ", LINE_TEXT VARCHAR(MAX) )"
                );

        targetTemplate.execute("IF OBJECT_ID('dbo.tblOutputDocs', 'U') IS NOT NULL DROP TABLE dbo.tblOutputDocs");        
        targetTemplate.execute("CREATE TABLE dbo.tblOutputDocs "
                + "( ID bigint IDENTITY(1,1) PRIMARY KEY"
                + ", DOC_ID bigint"
                + ", LINE_TEXT_CONCAT VARCHAR(MAX) )");
    }    
    

    
    private void insertTestLinesForDBLineFixer(DataSource ds){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        int docCount = 10;
        int lineCountIncrementer = 1;
        String sql = "INSERT INTO dbo.tblInputDocs "
                + "( DOC_ID"
                + ", LINE_ID"
                + ", LINE_TEXT"
                + ") VALUES (?,?,?)";        
        for (int i = 0; i <= docCount; i++) {
            for(int j = 0;j < lineCountIncrementer; j++){
                String text = "This is DOC_ID:" + i + " and LINE_ID:" + j ;
                jdbcTemplate.update(sql, i,j,text);
            }
            lineCountIncrementer++;
            if(lineCountIncrementer % 50 == 0){
                lineCountIncrementer = 0;
            }
        }                
    }
}
