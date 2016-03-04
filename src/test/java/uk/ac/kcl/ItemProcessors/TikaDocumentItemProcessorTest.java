/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.kcl.ItemProcessors;

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
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.model.BinaryDocument;

/**
 *
 * @author kcladmin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=JobConfiguration.class , loader=AnnotationConfigContextLoader.class)

public class TikaDocumentItemProcessorTest {
   
    /**
     * Test of process method, of class TikaDocumentItemProcessor.
     */
    @Autowired
    TikaDocumentItemProcessor instance;
    
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");        
        byte[] bytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("tika/testdocs/docexample.doc"));
        //String xhtmlString = new String(bytes,StandardCharsets.UTF_8);
        BinaryDocument doc = new BinaryDocument(bytes);
        //doc.getMetadata().put("xhtml", xhtmlString);
        BinaryDocument result = instance.process(doc);
        System.out.println(result.getMetadata().get("xhtml"));
        assertNotNull(result.getMetadata().get("xhtml"));
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
