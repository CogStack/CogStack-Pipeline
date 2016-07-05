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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.kcl.exception.BiolarkProcessingFailedException;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import java.util.*;

@Profile("biolark")
@Service("biolarkDocumentItemProcessor")
@ComponentScan("uk.ac.kcl.service")
public class BioLarkDocumentItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BioLarkDocumentItemProcessor.class);

    @Autowired
    private Environment env;
    private String endPoint;
    private String fieldName;
    private ObjectMapper mapper;
    @PostConstruct
    private void init(){

        fieldsToBioLark = Arrays.asList(env.getProperty("fieldsToBioLark").toLowerCase().split(","));
        endPoint = env.getProperty("biolarkEndPoint");
        setFieldName(env.getProperty("biolarkFieldName"));
         this.mapper = new ObjectMapper();
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
                Object json = null;
                try {
                    json = restTemplate.postForObject(endPoint, v, Object.class);
                }catch (HttpClientErrorException e){
                    LOG.warn("Biolark failed on document "+ doc.getDocName(), e);
                    ArrayList<LinkedHashMap<Object,Object>> al = new ArrayList<LinkedHashMap<Object, Object>>();
                    LinkedHashMap<Object,Object> hm = new LinkedHashMap<Object, Object>();
                    hm.put("error","see logs");
                    al.add(hm);
                    json = al;
                }
                newMap.put(fieldName,json);
            }
        });

        doc.getAdditionalFields().clear();
        doc.getAdditionalFields().putAll(newMap);
        return doc;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
