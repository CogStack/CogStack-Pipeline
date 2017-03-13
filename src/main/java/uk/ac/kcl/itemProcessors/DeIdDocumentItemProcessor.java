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

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.exception.DeIdentificationFailedException;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.service.ElasticGazetteerService;
import uk.ac.kcl.service.GateService;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Profile("deid")
@Service("deIdDocumentItemProcessor")
@ComponentScan("uk.ac.kcl.service")
public class DeIdDocumentItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeIdDocumentItemProcessor.class);

    @Autowired(required = false)
    private GateService gateService;

    @Autowired(required = false)
    private ElasticGazetteerService elasticGazetteer;

    @Autowired
    private Environment env;

    @Value("${deid.replaceFields}")
    private boolean replaceFields;

    @Value("${deid.useGateApp:#{false}}")
    private boolean useGateApp;
    private List<String> fieldsToDeId;

    @PostConstruct
    private void init(){
        fieldsToDeId = Arrays.asList(env.getProperty("deid.fieldsToDeId").toLowerCase().split(","));
    }





    public void setFieldsToDeId(List<String> fields){
        this.fieldsToDeId = fields;
    }


    @Override
    public Document process(final Document doc)  {
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(doc.getAssociativeArray());
        doc.getAssociativeArray().forEach((k, v)->{
            String newString = "";
            if(fieldsToDeId.contains(k.toLowerCase())) {
                try {
                    if (useGateApp) {
                        newString = gateService.deIdentifyString(v.toString(), doc.getPrimaryKeyFieldValue());
                    } else {
                        newString = elasticGazetteer.deIdentifyDates(v.toString(), doc.getPrimaryKeyFieldValue());
                        newString = elasticGazetteer.deIdentifyString(newString, doc.getPrimaryKeyFieldValue());
                    }
                    if(replaceFields) {
                        newMap.put(k, newString);
                    }else{
                        newMap.put(("deidentified_"+k), newString);
                    }
                }catch (DeIdentificationFailedException e){
                    LOG.warn("De-identification failed on document " + doc.getDocName(), e);
                    if(replaceFields) {
                        newMap.put(k,e.getLocalizedMessage());
                    }else{
                        newMap.put(("deidentified_"+k), e.getLocalizedMessage());
                    }
                }
            }
        });

        doc.getAssociativeArray().clear();
        doc.getAssociativeArray().putAll(newMap);
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }
}
