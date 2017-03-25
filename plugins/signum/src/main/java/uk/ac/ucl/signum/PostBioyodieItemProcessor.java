package uk.ac.ucl.signum;

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;

@Profile("postbioyodie")
@Service("PostBioyodieItemProcessor")
public class PostBioyodieItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PostBioyodieItemProcessor.class);

    @Autowired
    Environment env;

    @PostConstruct
    public void init() {
      // LOG.info("init for PostBioyodieItemProcessor");
    }


    @Override
    public Document process(final Document doc) throws Exception {
      LOG.info("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
      doc.getAssociativeArray().put("make_some_changes", "Hello world");
      long startTime = System.currentTimeMillis();
      long endTime = System.currentTimeMillis();
      LOG.info("{};Time:{} ms",
               this.getClass().getSimpleName(),
               endTime - startTime);
      LOG.info("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
      return doc;
    }
}
