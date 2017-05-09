package uk.ac.kcl.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.listener.SkipListenerSupport;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import uk.ac.kcl.model.Document;

@Service
@Qualifier("skipListener")
public class SkipListener extends SkipListenerSupport<Document, Document> {

    private static final Logger log = LoggerFactory.getLogger(SkipListener.class);

    @Autowired(required = false)
    @Qualifier("esRestDocumentWriter")
    ItemWriter<Document> esRestItemWriter;

    @Autowired
    public Environment env;

    @Override
    public void onSkipInRead(java.lang.Throwable t) {
        // TODO how to obtain the primary key if read fails?
        log.warn("Item skipped in read. Exception thrown: {}", t);
    }

    @Override
    public void onSkipInProcess(Document item, java.lang.Throwable t) {
        log.warn("SkipListener: Document PK: {} has been skipped in process() due to {}",
                 item.getPrimaryKeyFieldValue(),
                 t.getClass().getSimpleName(), t);

        // Put a placeholder in ES for documents failed processing
        // (so it gives the front-end app a chance to tell user about the existence of that document even though it failed processing)
        if (env.acceptsProfiles("placeholderForFailedDoc") && esRestItemWriter != null) {
          ArrayList<Document> docs = new ArrayList<Document>();
          // This flag signifies that the document is likely to be incomplete
          item.getAssociativeArray().put("X-TL-IS-PLACE-HOLDER", "true");
          item.setOutputData(item.getGson().toJson(item.getAssociativeArray()));
          docs.add(item);
          try {
            esRestItemWriter.write(docs);
            log.info("Placeholder for failed document (PK: {}) added to elastic search", item.getPrimaryKeyFieldValue());
          } catch (Exception e) {
            log.warn("Cannot not put placeholder for failed document (PK: {}) to elastic search", item.getPrimaryKeyFieldValue());
          }
        }
    }

    @Override
    public void onSkipInWrite(Document item, java.lang.Throwable t) {
        log.warn("SkipListener: Document PK: {} has been skipped in write() due to {}",
                 item.getPrimaryKeyFieldValue(),
                 t.getClass().getSimpleName(), t);
    }
}
