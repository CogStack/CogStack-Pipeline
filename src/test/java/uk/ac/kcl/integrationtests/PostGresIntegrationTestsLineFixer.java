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
import java.util.logging.Level;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.After;
import org.junit.Before;
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

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource({
		"classpath:postgres_test_config_line_fixer.properties",
                "classpath:jms.properties",
                "classpath:concurrency.properties",
                "classpath:dBLineFixer.properties",  
                "classpath:postgres_db.properties",                                                
                "classpath:step.properties"})
@ContextConfiguration(classes = {
    JobConfiguration.class,
    BatchConfigurer.class},
        loader = AnnotationConfigContextLoader.class)
public class PostGresIntegrationTestsLineFixer  {

    final static Logger logger = Logger.getLogger(PostGresIntegrationTestsLineFixer.class);

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

    
    //@Ignore
    @Test
    public void postgresDBLineFixerPipelineTest() {
        
        initPostGresJobRepository();
        initPostgresMultiLineTextTable();
        insertTestLinesForDBLineFixer(sourceDataSource);

        try {
            jobOperator.startNextInstance("dBLineFixerJob");
        } catch (NoSuchJobException | JobParametersNotFoundException | JobRestartException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | UnexpectedJobExecutionException | JobParametersInvalidException ex) {
            java.util.logging.Logger.getLogger(PostGresIntegrationTestsLineFixer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
        
    
    private void initPostgresMultiLineTextTable(){
        sourceTemplate.execute("DROP TABLE IF EXISTS tblInputDocs");
        sourceTemplate.execute("CREATE TABLE tblInputDocs"
                + "( ID  SERIAL PRIMARY KEY"
                + ", DOC_ID integer "
                + ", LINE_ID integer "
                + ", LINE_TEXT text )"
                );

        targetTemplate.execute("DROP TABLE IF EXISTS  tblOutputDocs");
        targetTemplate.execute("CREATE TABLE tblOutputDocs "
                + "( ID  SERIAL PRIMARY KEY"
                + ", DOC_ID integer"
                + ", LINE_TEXT_CONCAT text )");
    }
    
    
    private void initPostGresJobRepository(){
        dropTablesResource = new ClassPathResource("org/springframework/batch/core/schema-drop-postgresql.sql");
        makeTablesResource = new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql");
        rdp.addScript(dropTablesResource);
        rdp.addScript(makeTablesResource);
        rdp.execute(jdbcTargetDocumentFinder);        
    }
      

    
    private void insertTestLinesForDBLineFixer(DataSource ds){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        int docCount = 10;
        int lineCountIncrementer = 1;
        String sql = "INSERT INTO tblInputDocs "
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
