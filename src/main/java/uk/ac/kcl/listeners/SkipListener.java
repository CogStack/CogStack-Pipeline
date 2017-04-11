package uk.ac.kcl.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.listener.SkipListenerSupport;
import org.springframework.stereotype.Component;

import uk.ac.kcl.model.Document;

@Component
public class SkipListener extends SkipListenerSupport<Document, Document> {

    private static final Logger log = LoggerFactory.getLogger(SkipListener.class);

    @Override
    public void onSkipInRead(java.lang.Throwable t) {
        // TODO how to obtain the primary key if read fails?
    }

    @Override
    public void onSkipInProcess(Document item, java.lang.Throwable t) {
        log.warn("SkipListener: Document PK: {} has been skipped in process() due to {}",
                 item.getPrimaryKeyFieldValue(),
                 t.getClass().getSimpleName());
    }

    @Override
    public void onSkipInWrite(Document item, java.lang.Throwable t) {
        log.warn("SkipListener: Document PK: {} has been skipped in write() due to {}",
                 item.getPrimaryKeyFieldValue(),
                 t.getClass().getSimpleName());
    }
}
