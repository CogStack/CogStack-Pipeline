/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.kcl.ItemProcessors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.batch.item.ItemProcessor;
import org.xml.sax.ContentHandler;
import uk.ac.kcl.model.BinaryDocument;

/**
 *
 * @author rich
 */
public class TikaDocumentItemProcessor implements ItemProcessor<BinaryDocument, BinaryDocument> {

    private static final Logger logJdbcPath = Logger.getLogger(TikaDocumentItemProcessor.class);

    private boolean keepTags;

    public boolean isKeepTags() {
        return keepTags;
    }

    public void setKeepTags(boolean keepTags) {
        this.keepTags = keepTags;
    }

    @Override
    public BinaryDocument process(final BinaryDocument doc) throws Exception {
        ContentHandler handler;
        System.out.println("Processing Doc");

        if (keepTags) {
            handler = new ToXMLContentHandler();
        } else {
            handler = new BodyContentHandler();
        }
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        try (InputStream stream = new ByteArrayInputStream(doc.getBody())) {
            parser.parse(stream, handler, metadata);
            doc.getMetadata().put("xhtml", handler.toString());
        } catch (Exception ex) {
            doc.getMetadata().put("xhtml", ex.getMessage());
        }
        return doc;
    }


}
