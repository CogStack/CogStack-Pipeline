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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;
import uk.ac.kcl.scheduling.SingleJobLauncher;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource({
        "classpath:sql_server_test_config_tika.properties",
        "classpath:jms.properties",
        "classpath:concurrency.properties",
        "classpath:sql_server_db.properties",
        "classpath:tika.properties",
        "classpath:elasticsearch.properties",
        "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        ScheduledJobLauncher.class,
        SqlServerTestUtils.class,
        TestUtils.class},
        loader = AnnotationConfigContextLoader.class)
public class SqlServerIntegrationTestsTikaScheduling {

    final static Logger logger = Logger.getLogger(PostGresIntegrationTestsTikaScheduling.class);

    @Autowired
    SingleJobLauncher jobLauncher;

    @Autowired
    SqlServerTestUtils sqlServerTestUtils;

    @Autowired
    TestUtils testUtils;
    @Before
    public void init(){
        sqlServerTestUtils.initJobRepository();
        sqlServerTestUtils.initTikaTable();
        testUtils.insertTestBinariesForTika("dbo.tblInputDocs");
    }

    @Test
    @DirtiesContext
    public void sqlServerTikaPipelineTest() {
        try {
            Thread.sleep(1000000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
