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

import uk.ac.kcl.batch.TestJobConfiguration;
//import io.bluecell.data.JDBCDocumentSource;
//import io.bluecell.data.JDBCDocumentTarget;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:testApplicationContext.xml"})
@ContextConfiguration(classes = TestJobConfiguration.class, 
        loader = AnnotationConfigContextLoader.class)
public class JobIntegrationTests {

    final static Logger logger = Logger.getLogger(JobIntegrationTests.class);

    @Autowired
    @Qualifier("sourceDataSource")
    public DataSource jdbcSourceDocumentFinder;

    @Autowired
    @Qualifier("targetDataSource") 
    public DataSource jdbcTargetDocumentFinder;

    private static Server server1;
    private static Server server2;
        JdbcTemplate sourceTemplate;
        JdbcTemplate targetTemplate ;

    @BeforeClass
    public static void init() throws IOException, ServerAcl.AclFormatException {
        HsqlProperties p1 = new HsqlProperties();
        p1.setProperty("server.database.0", "mem:hsqldb");
        p1.setProperty("server.dbname.0", "test");
        p1.setProperty("server.port", "9001");
        p1.setProperty("server.remote_open", "true");
        server1 = new Server();
        server1.setProperties(p1);
        server1.setLogWriter(null);
        server1.setErrWriter(null);
        server1.start();

        HsqlProperties p2 = new HsqlProperties();
        p2.setProperty("server.database.0", "mem:hsqldb");
        p2.setProperty("server.dbname.0", "test2");
        p2.setProperty("server.port", "9002");
        p2.setProperty("server.remote_open", "true");
        server2 = new Server();
        server2.setProperties(p2);
        server2.setLogWriter(null);
        server2.setErrWriter(null);
        server2.start();
        
        //yodieconfig
        //Properties prop = System.getProperties();
        //prop.setProperty("at.ofai.gate.modularpipelines.configFile", "/home/rich/gate-apps/yodie/yodie-pipeline/main-bio/main-bio.config.yaml");        
    }

    @AfterClass
    public static void end() {
        server1.stop();
        server2.stop();
    }

    @Before
    public void initDb() {                
        sourceTemplate = new JdbcTemplate(jdbcSourceDocumentFinder);
        targetTemplate = new JdbcTemplate(jdbcTargetDocumentFinder);
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        Resource dropTablesResource;
        Resource makeTablesResource;        
//        //for MS SQL SERVER
//        jdbcSourceDocumentFinder.getJdbcTemplate().execute("CREATE TABLE tblInputDocs"
//                + " (srcColumnFieldName VARCHAR(100) "
//                + ", srcTableName VARCHAR(100) "
//                + ", primaryKeyFieldName VARCHAR(100) "
//                + ", primaryKeyFieldValue VARCHAR(100) "            
//                + ", binaryFieldName VARCHAR(100) "                            
//                + ", updateTime VARCHAR(100) "                 
//                + ", body VARBINARY(MAX) )");
//
//        //forHsql
//        jdbcTargetDocumentFinder.getJdbcTemplate().execute("CREATE TABLE tblOutputDocs "
//                + "( srcColumnFieldName VARCHAR(100) "
//                + ", srcTableName VARCHAR(100) "
//                + ", primaryKeyFieldName VARCHAR(100) "
//                + ", primaryKeyFieldValue VARCHAR(100) "            
//                + ", binaryFieldName VARCHAR(100) "                            
//                + ", updateTime VARCHAR(100) "                 
//                + ", xhtml VARCHAR(MAX) )");        
        
////        for postgres
        //sourceTemplate.execute("DROP TABLE tblInputDocs");
//        sourceTemplate.execute("CREATE TABLE tblInputDocs"
//                + "( ID  SERIAL PRIMARY KEY"
//                + ", srcColumnFieldName text "
//                + ", srcTableName text "
//                + ", primaryKeyFieldName text "
//                + ", primaryKeyFieldValue text "            
//                + ", binaryFieldName text "                            
//                + ", updateTime text "                 
//                + ", xhtml text )");

        targetTemplate.execute("DROP TABLE tblOutputDocs");        
        targetTemplate.execute("CREATE TABLE tblOutputDocs "
                + "( ID  SERIAL PRIMARY KEY"
                + ", srcColumnFieldName text "
                + ", srcTableName text "
                + ", primaryKeyFieldName text "
                + ", primaryKeyFieldValue text "            
                + ", binaryFieldName text "                            
                + ", updateTime text "                 
                + ", gatejson text )");        

        dropTablesResource = new ClassPathResource("org/springframework/batch/core/schema-drop-postgresql.sql") ;               
        makeTablesResource = new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql") ;       
        
////        //forhsql
//        sourceTemplate.execute("CREATE TABLE tblInputDocs"
//                + " (ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY"
//                + ", srcColumnFieldName VARCHAR(100) "
//                + ", srcTableName VARCHAR(100) "
//                + ", primaryKeyFieldName VARCHAR(100) "
//                + ", primaryKeyFieldValue VARCHAR(100) "            
//                + ", binaryFieldName VARCHAR(100) "                            
//                + ", updateTime VARCHAR(100) "                 
//                + ", xhtml VARCHAR(1500000))");
//
//        //forHsql
//        targetTemplate.execute("CREATE TABLE tblOutputDocs "
//                + " (ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 100, INCREMENT BY 1) PRIMARY KEY"
//                + ", srcColumnFieldName VARCHAR(100) "
//                + ", srcTableName VARCHAR(100) "
//                + ", primaryKeyFieldName VARCHAR(100) "
//                + ", primaryKeyFieldValue VARCHAR(100) "            
//                + ", binaryFieldName VARCHAR(100) "                            
//                + ", updateTime VARCHAR(100) "                 
//                + ", gateJSON VARCHAR(1500000) )");
//
//        dropTablesResource = new ClassPathResource("org/springframework/batch/core/schema-drop-hsqldb.sql") ;          
//        makeTablesResource = new ClassPathResource("org/springframework/batch/core/schema-hsqldb.sql");
        
        
        rdp.addScript(dropTablesResource);
        rdp.addScript(makeTablesResource);        
        rdp.execute(jdbcTargetDocumentFinder);
        
        
        //insertTestXHTML(jdbcSourceDocumentFinder, false);

    }

    @After
    public void dropDb() {
//        sourceTemplate.execute("DROP TABLE tblInputDocs");
//        targetTemplate.execute("DROP TABLE tblOutputDocs");
    }

    //remove ignore annotation to show!
    //@Ignore


    @Autowired
    JobOperator jobOperator;

    @Test
    public void gatePipelineTest() {    
        try {
        jobOperator.startNextInstance("gateJob");        
        } catch (NoSuchJobException | JobParametersNotFoundException | JobRestartException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | UnexpectedJobExecutionException | JobParametersInvalidException ex) {
            java.util.logging.Logger.getLogger(JobIntegrationTests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
    private void insertTestXHTML(DataSource ds, boolean includeGateBreaker) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        int docCount = 100;
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("xhtml_test"));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(JobIntegrationTests.class.getName()).log(Level.SEVERE, null, ex);
        }
        String xhtmlString = new String(bytes,StandardCharsets.UTF_8);
        
        String sql = "INSERT INTO tblInputDocs "
                + "( srcColumnFieldName"
                + ", srcTableName"
                + ", primaryKeyFieldName"
                + ", primaryKeyFieldValue"
                + ", updateTime"
                + ", xhtml"
                + ") VALUES (?,?,?,?,?,?)";
        for (int ii=0; ii<docCount;ii++) {
            //jdbcTemplate.update(sql, "fictionalColumnFieldName","fictionalTableName","fictionalPrimaryKeyFieldName", ii,null, xhtmlString);
            jdbcTemplate.update(sql, "fictionalColumnFieldName","fictionalTableName","fictionalPrimaryKeyFieldName", ii,null, ii);
        }
        //see what happens with a really long document...
        if(includeGateBreaker){
            try {
                bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("gate_breaker.txt"));
                xhtmlString = new String(bytes,StandardCharsets.UTF_8);
                jdbcTemplate.update(sql, "fictionalColumnFieldName","fictionalTableName","fictionalPrimaryKeyFieldName", docCount,null, xhtmlString);                
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(JobIntegrationTests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
