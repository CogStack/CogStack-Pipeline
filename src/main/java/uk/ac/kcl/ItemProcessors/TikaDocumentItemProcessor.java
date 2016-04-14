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
        logJdbcPath.debug("processing doc ID: " + doc.getId());
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
//        try (InputStream stream = new ByteArrayInputStream(doc.getBody())) {
//            parser.parse(stream, handler, metadata);
//            doc.setXhtml(handler.toString());
//        } catch (Exception ex) {
//            doc.setXhtml(ex.getMessage());
//        }
        return doc;
    }


}
