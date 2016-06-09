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
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.service.GateService;

import javax.annotation.PostConstruct;

@Profile("gate")
@Service("gateDocumentItemProcessor")
@ComponentScan("uk.ac.kcl.service")
public class GateDocumentItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GateDocumentItemProcessor.class);

    @Autowired
    private GateService gateService;

    @Autowired
    private Environment env;

    public void setGateService(GateService gateService) {
        this.gateService = gateService;
    }

    @PostConstruct
    public void init(){
        setFieldName(env.getProperty("gateFieldName"));
    }

    public GateService getGateService() {
        return gateService;
    }

    @Override
    public Document process(final Document doc) throws Exception {
        gate.Document gateDoc = Factory
                .newDocument((String) doc.getAdditionalFields()
                        .get(env.getProperty("gateInputFieldName")));
        try {
            gateService.processDoc(gateDoc);
            if(env.getProperty("gateJSON", "true").equalsIgnoreCase("true")){
                addField(doc, gateService.convertDocToJSON(gateDoc));
            }else{
                addField(doc, gateDoc.toXml());
            }
            return doc;
        }finally{
            Factory.deleteResource(gateDoc);
        }
    }
}
