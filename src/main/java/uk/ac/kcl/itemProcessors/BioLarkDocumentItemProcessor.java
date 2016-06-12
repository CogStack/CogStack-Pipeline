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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Profile("biolark")
@Service("biolarkDocumentItemProcessor")
@ComponentScan("uk.ac.kcl.service")
public class BioLarkDocumentItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BioLarkDocumentItemProcessor.class);

    @Autowired
    private Environment env;
    private String endPoint;

    @PostConstruct
    private void init(){

        fieldsToBioLark = Arrays.asList(env.getProperty("fieldsToBioLark").split(","));
        endPoint = env.getProperty("biolarkEndPoint");
    }

    private List<String> fieldsToBioLark;

    public void setFieldsToBioLark(List<String> fields){
        this.fieldsToBioLark = fields;
    }


    @Override
    public Document process(final Document doc)  {

        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(doc.getAdditionalFields());
        doc.getAdditionalFields().forEach((k,v)->{
            String newString = "";
            if(fieldsToBioLark.contains(k)) {
                RestTemplate restTemplate = new RestTemplate();
                String responseEntity = restTemplate.postForObject(endPoint,v,String.class);
                newMap.put((k+"_biolark"),responseEntity);
            }
        });

        doc.getAdditionalFields().clear();
        doc.getAdditionalFields().putAll(newMap);
        return doc;
    }
}
