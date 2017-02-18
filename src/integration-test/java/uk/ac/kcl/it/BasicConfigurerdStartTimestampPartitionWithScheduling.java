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
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;
import uk.ac.kcl.testexecutionlisteners.BasicTestExecutionListener;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
        "classpath:jms.properties",
        "classpath:postgres_test.properties",
        "classpath:postgres_db.properties",
//        "classpath:sql_server_test.properties",
//        "classpath:sql_server_db.properties",
        "classpath:scheduling.properties",
        "classpath:configured_start.properties",
        "classpath:elasticsearch.properties",
        "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        PostGresTestUtils.class,
        SqlServerTestUtils.class,
        ScheduledJobLauncher.class,
        TestUtils.class},
        loader = AnnotationConfigContextLoader.class)
@TestExecutionListeners(
        listeners = BasicTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ActiveProfiles({"basic","localPartitioning","jdbc_in","jdbc_out","elasticsearch","primaryKeyAndTimeStampPartition","postgres"})
//@ActiveProfiles({"basic","localPartitioning","jdbc_in","jdbc_out","elasticsearch","primaryKeyPartition","sqlserver"})
public class BasicConfigurerdStartTimestampPartitionWithScheduling {

    @Autowired
    private TestUtils testUtils;
    @Autowired
    DbmsTestUtils dbmsTestUtils;
    @Autowired
    Environment env;

    @Test
    @DirtiesContext
    public void basicConfigurerdStartTimestampWithSchedulingTest() {
        testUtils.insertFreshDataIntoBasicTableAfterDelay(env.getProperty("tblInputDocs"),15000);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //note, in this test, we upsert documents, overriding existng ones. hence why ther ate 75 in the index and 150
        //in the db
        assertEquals(75,testUtils.countOutputDocsInES());
        assertEquals(150,dbmsTestUtils.countRowsInOutputTable());
    }

}
