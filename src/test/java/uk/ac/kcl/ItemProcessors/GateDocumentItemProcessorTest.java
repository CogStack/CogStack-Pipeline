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

import uk.ac.kcl.model.BinaryDocument;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.batch.GateConfiguration;
import uk.ac.kcl.batch.JobConfiguration;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:unit_test_config_gate.properties")
@ContextConfiguration(classes = {
    GateConfiguration.class},
        loader = AnnotationConfigContextLoader.class)
public class GateDocumentItemProcessorTest {
   

    /**
     * Test of process method, of class GateDocumentItemProcessor.
     */
    
    @Autowired
    @Qualifier("gateItemProcessor")
    public GateDocumentItemProcessor instance;
    
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
