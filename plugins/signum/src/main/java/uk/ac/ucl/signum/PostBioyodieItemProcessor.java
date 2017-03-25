package uk.ac.ucl.signum;

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import uk.ac.kcl.model.Document;

import java.lang.ClassCastException;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

@Profile("postbioyodie")
@Service("PostBioyodieItemProcessor")
public class PostBioyodieItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PostBioyodieItemProcessor.class);

    @Autowired
    Environment env;

    @Value("${webservice.fieldName}")
    private String bioYodieFieldName;

    @PostConstruct
    public void init() {

    }


    @Override
    public Document process(final Document doc) throws Exception {
      LOG.info("Starting {} on doc", this.getClass().getSimpleName(), doc.getDocName());

      long startTime = System.currentTimeMillis();
      try {

        if (bioYodieFieldName != null) {
          // Map<String, Object> mentionList;
          Object bioyodieMapObj;
          Object entitiesMapObj;
          Object mentionListObj;
          bioyodieMapObj = doc.getAssociativeArray().getOrDefault(bioYodieFieldName, null);
          if (bioyodieMapObj != null) {
            entitiesMapObj = ((Map<String, Object>) bioyodieMapObj).getOrDefault("entities", null);
            if (entitiesMapObj != null) {
              mentionListObj = ((Map<String, Object>) entitiesMapObj).getOrDefault("Mention", null);
              if (mentionListObj != null) {
                doc.getAssociativeArray().put(bioYodieFieldName, mentionListObj);
                doc.getAssociativeArray().put("X-PLUGINS-POST-BIO-YODIE", "success");
              }
            }
          }
        }
        long endTime = System.currentTimeMillis();
        LOG.info("{};Time:{} ms",
                 this.getClass().getSimpleName(),
                 endTime - startTime);
        LOG.info("Finished {} on doc", this.getClass().getSimpleName(), doc.getDocName());
      } catch (ClassCastException castEx) {
        LOG.warn("ClassCastException caught, possibly due to malformed result");
      } catch (Exception e) {
        LOG.error("Exception caught {}", e);
      }
      finally {
        return doc;
      }
    }
}
