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

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 *
 * @author rich
 */
@Profile("tika")
@Service("tikaDocumentItemProcessor")
public class TikaDocumentItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TikaDocumentItemProcessor.class);

    private boolean keepTags;
    private String binaryFieldName;
    private AutoDetectParser parser;

    public boolean isKeepTags() {
        return keepTags;
    }

    public void setKeepTags(boolean keepTags) {
        this.keepTags = keepTags;
    }

    @Autowired
    Environment env;

    @PostConstruct
    public void init(){
        this.keepTags = env.getProperty("keepTags").equalsIgnoreCase("true");
        setFieldName(env.getProperty("tikaFieldName"));
        parser = new AutoDetectParser();
    }

    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        ContentHandler handler;
        if (keepTags) {
            handler = new ToXMLContentHandler();
        } else {
            handler = new BodyContentHandler();
        }
        ;
        Metadata metadata = new Metadata();
        try (InputStream stream = new ByteArrayInputStream(doc.getBinaryContent())) {
            parser.parse(stream, handler, metadata);
            addField(doc, handler.toString());
        } catch (Exception ex) {
            addField(doc,ex.getMessage());
        }
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }


}
