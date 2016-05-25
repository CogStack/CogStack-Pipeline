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
import org.springframework.core.env.Environment;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.TextDocument;
import uk.ac.kcl.service.GateService;

import java.util.ArrayList;
import java.util.List;


public class DeIdDocumentItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeIdDocumentItemProcessor.class);

    @Autowired
    private GateService gateService;

    @Autowired
    private Environment env;


    private List<String> fieldsToDeId;

    public void setFieldsToDeId(List<String> fields){
        this.fieldsToDeId = fields;
    }


    @Override
    public Document process(final Document doc) throws Exception {

        doc.getAdditionalFields().forEach((k,v)->{
            if(fieldsToDeId.contains(k)) {
                String newString = "unable to de-id";
                try {
                    newString = gateService.deIdentifyString(v.toString());
                } catch (ExecutionException|ResourceInstantiationException e) {
                    LOG.warn("Unable to deid field " + k + " in document " + doc.getDocName());
                }
                doc.getAdditionalFields().put(k,newString);
            }
        });
        return doc;
    }
}
