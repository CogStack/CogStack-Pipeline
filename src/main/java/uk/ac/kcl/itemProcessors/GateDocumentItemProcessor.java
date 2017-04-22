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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gate.Factory;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.service.GateService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Profile("gate")
@Service("gateDocumentItemProcessor")
@ComponentScan("uk.ac.kcl.service")
public class GateDocumentItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GateDocumentItemProcessor.class);

    @Autowired
    private GateService gateService;

    @Autowired
    private Environment env;
    private List<String> fieldsToGate;
    @Value("${gate.gateFieldName}")
    private String fieldName;
    private JsonParser jsonParser;

    public void setGateService(GateService gateService) {
        this.gateService = gateService;
    }

    @PostConstruct
    public void init(){
        fieldsToGate = Arrays.asList(env.getProperty("gate.fieldsToGate", "").toLowerCase().split(","));
        this.jsonParser = new JsonParser();
    }

    public GateService getGateService() {
        return gateService;
    }

    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        long startTime = System.currentTimeMillis();
        int contentLength = doc.getAssociativeArray().keySet()
                               .stream()
                               .filter(k -> fieldsToGate.contains(k.toLowerCase()))
                               .mapToInt(k -> ((String) doc.getAssociativeArray().get(k)).length())
                               .sum();

        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(doc.getAssociativeArray());
        List<String> failedFieldsList = new ArrayList<String>(fieldsToGate);

        newMap.put(fieldName, new HashMap<String,Object>());

        doc.getAssociativeArray().forEach((k, v)-> {
            if (fieldsToGate.contains(k.toLowerCase())) {
                gate.Document gateDoc = null;
                try {
                    gateDoc = Factory
                            .newDocument((String) v);
                    LOG.info("Going to process key: {} in document PK: {}, content length: {}",
                             k, doc.getPrimaryKeyFieldValue(), ((String) v).length());
                    gateService.processDoc(gateDoc);
                    ((HashMap<String,Object>) newMap.get(fieldName)).put(k, gateService.convertDocToJSON(gateDoc));

                    // Remove the key from the list if GATE is successful
                    failedFieldsList.remove(k.toLowerCase());
                } catch (Exception e) {
                    LOG.warn("gate failed on doc {} (PK: {}): {}", doc.getDocName(), doc.getPrimaryKeyFieldValue(), e);
                    ArrayList<LinkedHashMap<Object, Object>> al = new ArrayList<LinkedHashMap<Object, Object>>();
                    LinkedHashMap<Object, Object> hm = new LinkedHashMap<Object, Object>();
                    hm.put("error", "see logs");
                    al.add(hm);
                    ((HashMap<String,Object>) newMap.get(fieldName)).put(k, hm);
                } finally {
                    Factory.deleteResource(gateDoc);
                }
            }
        });
        if (failedFieldsList.size() == 0) {
            newMap.put("X-TL-GATE", "Success");
        } else {
            newMap.put("X-TL-GATE", "Failed fields: " + String.join(", ", failedFieldsList));
        }
        doc.getAssociativeArray().clear();
        doc.getAssociativeArray().putAll(newMap);
        long endTime = System.currentTimeMillis();
        LOG.info("{};Primary-Key:{};Total-Content-Length:{};Time:{} ms",
                 this.getClass().getSimpleName(),
                 doc.getPrimaryKeyFieldValue(),
                 contentLength,
                 endTime - startTime);
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }
}
