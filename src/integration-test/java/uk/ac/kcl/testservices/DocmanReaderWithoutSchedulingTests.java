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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.kcl.scheduling.SingleJobLauncher;
import uk.ac.kcl.utils.DbmsTestUtils;
import uk.ac.kcl.utils.TestUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author rich
 */
@Service
@ComponentScan("uk.ac.kcl.it")
@Ignore
public class DocmanReaderWithoutSchedulingTests {

    final static Logger logger = Logger.getLogger(DocmanReaderWithoutSchedulingTests.class);

    @Autowired
    SingleJobLauncher jobLauncher;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    DbmsTestUtils dbmsTestUtils;
    @Autowired
    Environment env;

    @Test
    @DirtiesContext
    public void docmanReaderTest() {
        jobLauncher.launchJob();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(2,testUtils.countOutputDocsInES());
        assertEquals(2,dbmsTestUtils.countRowsInOutputTable());

        String testString = testUtils.getStringInEsDoc("1");
        JsonObject json = new JsonParser().parse(testString).getAsJsonObject();
        String docStringPath1 = json.get("_source").getAsJsonObject().get("path").getAsString();

        testString = testUtils.getStringInEsDoc("2");
        json = new JsonParser().parse(testString).getAsJsonObject();
        String docStringPath2 = json.get("_source").getAsJsonObject().get("path").getAsString();

        Tika tika = new Tika();

        String doc1 = null;
        String doc2 = null;
        try {
            doc1 = tika.parseToString(getClass().getClassLoader().getResourceAsStream(docStringPath1));
            doc2 = tika.parseToString(getClass().getClassLoader().getResourceAsStream(docStringPath2));
        } catch (TikaException | IOException e) {
            logger.error("Cannot read the document at the specified path");
            e.printStackTrace();
        }

        assertTrue(doc1
                .contains("The patient’s name is Bart Davidson"));
        assertTrue(doc2
                .contains("The patient’s name is David Harleyson"));

    }



}
