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

import org.apache.log4j.Logger;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.batch.BasicJobConfiguration;
import uk.ac.kcl.batch.BatchConfigurer;
import uk.ac.kcl.batch.JobConfiguration;

import java.util.logging.Level;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
    "classpath:postgres_test_config_basic.properties",
    "classpath:jms.properties",
    "classpath:concurrency.properties",
    "classpath:sql_server_db.properties",
    "classpath:elasticsearch.properties",
    "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
    JobConfiguration.class,
    BatchConfigurer.class,
    BasicJobConfiguration.class,
    SqlServerTestUtils.class},
        loader = AnnotationConfigContextLoader.class)
public class SqlServerIntegrationTestsBasic {

    final static Logger logger = Logger.getLogger(SqlServerIntegrationTestsBasic.class);

    @Autowired
    JobOperator jobOperator;

    @Autowired
    SqlServerTestUtils utils;

    @Test
    public void sqlServerBasicPipelineTest() {
//        utils.createBasicInputTable();
//        utils.createBasicOutputTable();
        utils.initJobRepository();
//        utils.insertDataIntoBasicTable();
        try {
            jobOperator.startNextInstance("basicJob");
        } catch (NoSuchJobException | JobParametersNotFoundException | JobRestartException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | UnexpectedJobExecutionException | JobParametersInvalidException ex) {
            java.util.logging.Logger.getLogger(SqlServerIntegrationTestsBasic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
