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
package uk.ac.kcl.itemProcessors;

import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.TextDocument;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=JobConfiguration.class , loader=AnnotationConfigContextLoader.class)
public class GateDocumentItemProcessorTest {
    
    public GateDocumentItemProcessorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of process method, of class GateDocumentItemProcessor.
     */
    @Autowired
    GateDocumentItemProcessor instance ;
          
    @Ignore
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");        
        byte[] bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("xhtml_test"));
        String xhtmlString = new String(bytes,StandardCharsets.UTF_8);
        Document doc = new Document();
        doc.setTextContent(xhtmlString);
        Document result = instance.process(doc);
        System.out.println(result.getOutputData());
        assertNotNull(result.getOutputData());
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
