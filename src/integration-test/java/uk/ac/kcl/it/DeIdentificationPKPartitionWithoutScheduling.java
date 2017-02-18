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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.mutators.Mutant;
import uk.ac.kcl.rowmappers.DocumentRowMapper;
import uk.ac.kcl.scheduling.SingleJobLauncher;
import uk.ac.kcl.service.ElasticGazetteerService;
import uk.ac.kcl.testexecutionlisteners.DeidTestExecutionListener;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
        "classpath:postgres_test.properties",
        "classpath:postgres_db.properties",
//        "classpath:sql_server_test.properties",
//        "classpath:sql_server_db.properties",
        "classpath:jms.properties",
        "classpath:noScheduling.properties",
        "classpath:gate.properties",
        "classpath:deidentification.properties",
        "classpath:elasticsearch.properties",
        "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        SingleJobLauncher.class,
        PostGresTestUtils.class,
        SqlServerTestUtils.class,
        TestUtils.class},
        loader = AnnotationConfigContextLoader.class)
@TestExecutionListeners(
        listeners = DeidTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ActiveProfiles({"deid","basic","localPartitioning","jdbc_in","jdbc_out","elasticsearch","primaryKeyPartition","postgres"})
//@ActiveProfiles({"deid","basic","localPartitioning","jdbc_in","jdbc_out","elasticsearch","primaryKeyPartition","sqlserver"})
public class DeIdentificationPKPartitionWithoutScheduling {

    final static Logger logger = Logger.getLogger(DeIdentificationPKPartitionWithoutScheduling.class);

    @Autowired
    SingleJobLauncher jobLauncher;

    @Autowired
    TestUtils testUtils;

    @Autowired
    DbmsTestUtils dbmsTestUtils;

    @Test
    @DirtiesContext
    public void deIdentificationTest() {

        jobLauncher.launchJob();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(106,testUtils.countOutputDocsInES());
        assertEquals(106,dbmsTestUtils.countRowsInOutputTable());
    }




}
