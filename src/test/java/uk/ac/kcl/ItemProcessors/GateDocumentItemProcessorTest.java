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
package uk.ac.kcl.ItemProcessors;

import uk.ac.kcl.ItemProcessors.GateDocumentItemProcessor;
import uk.ac.kcl.batch.TestJobConfiguration;
import uk.ac.kcl.model.BinaryDocument;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=TestJobConfiguration.class , loader=AnnotationConfigContextLoader.class)
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
            
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");        
        byte[] bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("xhtml_test"));
        String xhtmlString = new String(bytes,StandardCharsets.UTF_8);
        BinaryDocument doc = new BinaryDocument();
        doc.getMetadata().put("xhtml", xhtmlString);
        BinaryDocument result = instance.process(doc);
        System.out.println(result.getMetadata().get("gateJSON"));
        assertNotNull(result.getMetadata().get("gateJSON"));
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
