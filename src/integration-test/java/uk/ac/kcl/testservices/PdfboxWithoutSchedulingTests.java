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

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.kcl.scheduling.SingleJobLauncher;
import uk.ac.kcl.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author rich
 */
@Service
@ComponentScan("uk.ac.kcl.it")
@Ignore
public class PdfboxWithoutSchedulingTests {

    final static Logger logger = Logger.getLogger(PdfboxWithoutSchedulingTests.class);

    @Autowired
    SingleJobLauncher jobLauncher;
    @Autowired
    TestUtils testUtils;


    @Test
    @DirtiesContext
    public void pdfboxPipelineTest() {
        jobLauncher.launchJob();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(55, testUtils.countOutputDocsInES());

        String testString = testUtils.getStringInEsDoc("1");
        assertTrue(testString.contains("\"outputField1\":\"DUDE\""));
    }
}
