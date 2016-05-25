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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;
import uk.ac.kcl.scheduling.SingleJobLauncher;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
    "classpath:sqlserver_test_config_basic.properties",
    "classpath:jms.properties",
    "classpath:concurrency.properties",
    "classpath:basic.properties",
        "classpath:gate.properties",
    "classpath:sql_server_db.properties",
    "classpath:elasticsearch.properties",
    "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        SqlServerTestUtils.class,
        ScheduledJobLauncher.class,
        TestUtils.class},
        loader = AnnotationConfigContextLoader.class)
public class SqlServerIntegrationTestsBasicScheduling {

    final static Logger logger = Logger.getLogger(PostGresIntegrationTestsBasicScheduling.class);

    @Autowired
    SingleJobLauncher jobLauncher;

    @Autowired
    SqlServerTestUtils sqlServerTestUtils;

    @Autowired
    TestUtils testUtils;
    @Before
    public void init(){
        sqlServerTestUtils.initJobRepository();
        sqlServerTestUtils.createBasicInputTable();
        sqlServerTestUtils.createBasicOutputTable();
        testUtils.insertDataIntoBasicTable("dbo.tblInputDocs");
    }
    @Test
    public void sqlServerBasicSchedulingPipelineTest() {
        try {
            Thread.sleep(1000000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }

}