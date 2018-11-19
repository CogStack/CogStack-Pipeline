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
package uk.ac.kcl.testservices;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.kcl.utils.DbmsTestUtils;
import uk.ac.kcl.utils.TestUtils;

import static org.junit.Assert.assertEquals;

@Service
@ComponentScan("uk.ac.kcl.it")
@Ignore
public class BasicConfigWithSchedulingSmallInsertTests {

    @Autowired
    private TestUtils testUtils;
    @Autowired
    DbmsTestUtils dbmsTestUtils;
    @Autowired
    Environment env;

    @Test
    @DirtiesContext
    public void basicTimestampPartitionWithSchedulingTest() {
        testUtils.insertFreshDataIntoBasicTableAfterDelay(env.getProperty("tblInputDocs"),15000,5,8,false);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //note, in this test, we upsert documents, overriding existng ones. hence why there are 75 in the index and 150
        //in the db
        assertEquals(8,testUtils.countOutputDocsInES());
        assertEquals(8,dbmsTestUtils.countRowsInOutputTable());
    }

}
