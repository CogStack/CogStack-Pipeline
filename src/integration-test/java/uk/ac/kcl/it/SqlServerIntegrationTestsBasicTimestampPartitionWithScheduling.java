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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;
import uk.ac.uk.it.TestExecutionListeners.SqlServerBasicTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
        "classpath:basicPKAndTimeStampProfiles.properties",
        "classpath:sql_server_test_config_basic.properties",
        "classpath:jms.properties",
        "classpath:sql_server_db.properties",
        "classpath:elasticsearch.properties",
        "classpath:scheduling.properties",
        "classpath:biolark.properties",
        "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        SqlServerTestUtils.class,
        ScheduledJobLauncher.class,
        TestUtils.class},
        loader = AnnotationConfigContextLoader.class)
@TestExecutionListeners(
        listeners = SqlServerBasicTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class SqlServerIntegrationTestsBasicTimestampPartitionWithScheduling {

    @Autowired
    private TestUtils testUtils;

    @Test
    @DirtiesContext
    public void SqlServerIntegrationTestsBasicTimestampPartitionWithSchedulingTest() {
        testUtils.insertFreshDataIntoBasicTableAfterDelay("dbo.tblInputDocs",15000);
    }

}
