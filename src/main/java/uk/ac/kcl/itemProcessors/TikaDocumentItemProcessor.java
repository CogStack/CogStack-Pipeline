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

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import uk.ac.kcl.model.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;

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
    private TikaConfig config;

    public boolean isKeepTags() {
        return keepTags;
    }

    public void setKeepTags(boolean keepTags) {
        this.keepTags = keepTags;
    }

    @Autowired
    Environment env;

    @PostConstruct
    public void init() throws IOException, SAXException, TikaException{
        this.keepTags = env.getProperty("keepTags").equalsIgnoreCase("true");
        setFieldName(env.getProperty("tikaFieldName"));

        config = new TikaConfig(this.getClass().getClassLoader()
                                .getResourceAsStream("tika-config.xml"));
        parser = new AutoDetectParser(config);
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

        Metadata metadata = new Metadata();
        try (InputStream stream = new ByteArrayInputStream(doc.getBinaryContent())) {
            ParseContext context = new ParseContext();
            context.set(TikaConfig.class, config);
            parser.parse(stream, handler, metadata, context);

            Set<String> metaKeys = new HashSet<String>(Arrays.asList(
                                                          metadata.names()));

            extractOCRMetadata(doc, metaKeys, metadata);

            extractContentTypeMetadata(doc, metaKeys, metadata);

            extractPageCountMetadata(doc, metaKeys, metadata);

            addField(doc, handler.toString());
        } catch (Exception ex) {
            addField(doc, ex.getMessage());
        }
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }

    private void extractOCRMetadata(Document doc, Set<String> metaKeys,
                                    Metadata metadata) {
        if (metaKeys.contains("X-PDFPREPROC-OCR-APPLIED")) {
            doc.getAssociativeArray().put("X-PDFPREPROC-OCR-APPLIED",
                metadata.get("X-PDFPREPROC-OCR-APPLIED"));
        }
        if (metaKeys.contains("X-PDFPREPROC-ORIGINAL")) {
            doc.getAssociativeArray().put("X-PDFPREPROC-ORIGINAL",
                metadata.get("X-PDFPREPROC-ORIGINAL"));
        }
    }

    private void extractContentTypeMetadata(Document doc, Set<String> metaKeys,
                                            Metadata metadata) {
        if (metaKeys.contains("Content-Type")) {
            doc.getAssociativeArray().put("X-TL-CONTENT-TYPE",
                metadata.get("Content-Type"));
        } else {
            doc.getAssociativeArray().put("X-TL-CONTENT-TYPE",
                "TL_CONTENT_TYPE_UNKNOWN");
        }
    }

    private void extractPageCountMetadata(Document doc, Set<String> metaKeys,
                                          Metadata metadata) {
        if (metaKeys.contains("xmpTPg:NPages")) {
            doc.getAssociativeArray().put("X-TL-PAGE-COUNT",
                metadata.get("xmpTPg:NPages"));

        } else if (metaKeys.contains("Page-Count")) {
            doc.getAssociativeArray().put("X-TL-PAGE-COUNT",
                metadata.get("Page-Count"));

        } else if (metaKeys.contains("meta:page-count")) {
            doc.getAssociativeArray().put("X-TL-PAGE-COUNT",
                metadata.get("meta:page-count"));

        } else {
            doc.getAssociativeArray().put("X-TL-PAGE-COUNT",
                "TL_PAGE_COUNT_UNKNOWN");
        }
    }
}
