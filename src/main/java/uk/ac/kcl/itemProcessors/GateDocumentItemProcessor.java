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

import gate.Factory;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Boolean jsonOutput;
    private String fieldName;

    public void setGateService(GateService gateService) {
        this.gateService = gateService;
    }

    @PostConstruct
    public void init(){

        fieldsToGate = Arrays.asList(env.getProperty("fieldsToGate").toLowerCase().split(","));
        jsonOutput = Boolean.valueOf(env.getProperty("gateJSON"));
        fieldName = env.getProperty("gateFieldName");

    }

    public GateService getGateService() {
        return gateService;
    }

    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(doc.getAssociativeArray());
        doc.getAssociativeArray().forEach((k, v)-> {
            if (fieldsToGate.contains(k)) {
                gate.Document gateDoc = null;
                try {
                    gateDoc = Factory
                            .newDocument((String) v);
                    gateService.processDoc(gateDoc);
                    if (jsonOutput) {
                        newMap.put(fieldName, gateService.convertDocToJSON(gateDoc));
                    } else {
                        newMap.put(fieldName, gateDoc.toXml());
                    }

                } catch (ExecutionException | IOException | ResourceInstantiationException e) {
                    LOG.debug("gate failed on doc " + doc.getDocName() + " ", e);
                    LOG.warn("Biolark failed on document " + doc.getDocName());
                    ArrayList<LinkedHashMap<Object, Object>> al = new ArrayList<LinkedHashMap<Object, Object>>();

                    LinkedHashMap<Object, Object> hm = new LinkedHashMap<Object, Object>();
                    hm.put("error", "see logs");
                    al.add(hm);
                    newMap.put(fieldName,hm);

                } finally {
                    Factory.deleteResource(gateDoc);
                }
            }
        });
        doc.getAssociativeArray().clear();
        doc.getAssociativeArray().putAll(newMap);
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }
}
