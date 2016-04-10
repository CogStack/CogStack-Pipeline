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
package uk.ac.kcl.ItemProcessors;

import gate.Factory;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.service.GateService;
import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;


public class GateDocumentItemProcessor implements ItemProcessor<BinaryDocument, BinaryDocument> {

    private static final Logger logJdbcPath = Logger.getLogger(GateDocumentItemProcessor.class);

    @Autowired
    private GateService gateService;

    @Autowired
    private Environment env;

    public void setGateService(GateService gateService) {
        this.gateService = gateService;
    }

    public GateService getGateService() {
        return gateService;
    }

    @Override
    public BinaryDocument process(final BinaryDocument doc) throws Exception {
        gate.Document gateDoc = Factory.newDocument(doc.getMetadata().get(env.getProperty("textFieldName")));
        try {

            gateService.processDoc(gateDoc);
            if(env.getProperty("gateJSON", "true").equalsIgnoreCase("true")){
                doc.getMetadata().put("gateJSON", gateService.convertDocToJSON(gateDoc));
            }else{
                doc.getMetadata().put("gateJSON", gateDoc.toXml());                
            }
            return doc;
        }finally{
            Factory.deleteResource(gateDoc);
        }
    }
}
