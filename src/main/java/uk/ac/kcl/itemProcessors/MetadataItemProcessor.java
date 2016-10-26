package uk.ac.kcl.itemProcessors;

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;


@Profile("metadata")
@Service("metadataItemProcessor")
public class MetadataItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MetadataItemProcessor.class);

    private boolean eagerGetPageCount;
    private String binaryFieldName;

    @Autowired
    Environment env;

    @PostConstruct
    public void init() {
        this.eagerGetPageCount = env.getProperty("eagerGetPageCount")
                                 .equalsIgnoreCase("true");
    }

    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() + " on doc " +doc.getDocName());
        Map<String, Object> associativeArray = doc.getAssociativeArray();
        if (eagerGetPageCount) {
            boolean isTiff = (
                (String) associativeArray.getOrDefault("X-TL-CONTENT-TYPE", "")
                ).equalsIgnoreCase("image/tiff");
            boolean isPageCountUnknown = (
                (String) associativeArray.getOrDefault("X-TL-PAGE-COUNT", "TL_PAGE_COUNT_UNKNOWN")
                ).equals("TL_PAGE_COUNT_UNKNOWN");

            if (isTiff && isPageCountUnknown) {
                associativeArray.put("X-TL-PAGE-COUNT", "PLACEHOLDER");
            }
        }
        LOG.debug("finished " + this.getClass().getSimpleName() + " on doc " +doc.getDocName());
        return doc;
    }

}
