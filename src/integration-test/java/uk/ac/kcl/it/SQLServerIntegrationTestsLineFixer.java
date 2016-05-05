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
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.scheduling.SingleJobLauncher;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
        "classpath:sqlserver_test_config_line_fixer.properties",
        "classpath:jms.properties",
        "classpath:dBLineFixer.properties",
        "classpath:concurrency.properties",
        "classpath:sql_server_db.properties",
        "classpath:elasticsearch.properties",
        "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        SingleJobLauncher.class,
        SqlServerTestUtils.class},
        loader = AnnotationConfigContextLoader.class)
public class SQLServerIntegrationTestsLineFixer{

    final static Logger logger = Logger.getLogger(SQLServerIntegrationTestsLineFixer.class);

    @Autowired
    SingleJobLauncher jobLauncher;
    @Autowired
    SqlServerTestUtils utils;



    //@Ignore
    @Test
    public void postgresGatePipelineTest() {
        utils.initJobRepository();
        utils.initMultiLineTextTable();
        utils.insertTestLinesForDBLineFixer();
        jobLauncher.launchJob();
    }


}
