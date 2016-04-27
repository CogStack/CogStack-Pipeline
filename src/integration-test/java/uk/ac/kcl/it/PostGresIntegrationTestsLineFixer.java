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
package uk.ac.kcl.it;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import uk.ac.kcl.batch.JobConfiguration;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
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
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
        "classpath:postgres_test_config_line_fixer.properties",
        "classpath:jms.properties",
        "classpath:concurrency.properties",
        "classpath:dBLineFixer.properties",
        "classpath:elasticsearch.properties",
        "classpath:postgres_db.properties",
        "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        JobConfiguration.class,
        BatchConfigurer.class,
        PostGresTestUtils.class},
        loader = AnnotationConfigContextLoader.class)
public class PostGresIntegrationTestsLineFixer  {

    final static Logger logger = Logger.getLogger(PostGresIntegrationTestsLineFixer.class);

    @Autowired
    JobOperator jobOperator;

    @Autowired
    Environment env;

    @Autowired
    PostGresTestUtils utils;

    @Test
    public void postgresDBLineFixerPipelineTest() {
        utils.initPostGresJobRepository();
        utils.initPostgresMultiLineTextTable();
        utils.insertTestLinesForDBLineFixer();
        try {
            jobOperator.startNextInstance("dBLineFixerJob");
        } catch (NoSuchJobException | JobParametersNotFoundException | JobRestartException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | UnexpectedJobExecutionException | JobParametersInvalidException ex) {
            java.util.logging.Logger.getLogger(PostGresIntegrationTestsLineFixer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
