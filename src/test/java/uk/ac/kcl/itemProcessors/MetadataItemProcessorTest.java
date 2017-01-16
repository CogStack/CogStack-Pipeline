package uk.ac.kcl.itemProcessors;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.kcl.itemProcessors.MetadataItemProcessor;
import uk.ac.kcl.model.Document;

public class MetadataItemProcessorTest {

    @Test
    public void testcountPageInTiff() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("tika/testdocs/tiff_multi_pages.tiff");
        Document doc = new Document();
        doc.setSrcTableName("dummySrcTable");
        doc.setSrcColumnFieldName("dummySrcColumnField");
        doc.setPrimaryKeyFieldValue("dummyPrimaryKeyField");
        doc.setBinaryContent(IOUtils.toByteArray(stream));

        MetadataItemProcessor proc = new MetadataItemProcessor();
        proc.countPageInTiff(doc);
        String pageCountStr = (String) doc.getAssociativeArray().get("X-TL-PAGE-COUNT");
        Assert.assertEquals(pageCountStr, "6");
    }
}
