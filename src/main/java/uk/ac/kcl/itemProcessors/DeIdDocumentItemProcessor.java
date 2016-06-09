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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.exception.DeIdentificationFailedException;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.service.ElasticGazetteerService;
import uk.ac.kcl.service.GateService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
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

    @PostConstruct
    private void init(){
        fieldsToDeId = Arrays.asList(env.getProperty("fieldsToDeId").split(","));
    }

    private List<String> fieldsToDeId;

    public void setFieldsToDeId(List<String> fields){
        this.fieldsToDeId = fields;
    }


    @Override
    public Document process(final Document doc)  {

        doc.getAdditionalFields().forEach((k,v)->{
            String newString = "";
            if(fieldsToDeId.contains(k)) {
                if(env.getProperty("useGateApp").equalsIgnoreCase("true")) {
                    newString = gateService.deIdentifyString(v.toString(), doc.getPrimaryKeyFieldValue());
                }else{
                    newString = elasticGazetteer.deIdentify(v.toString(),doc.getPrimaryKeyFieldValue());
                }

                doc.getAdditionalFields().put(k,newString);
            }
        });
        return doc;
    }
}
