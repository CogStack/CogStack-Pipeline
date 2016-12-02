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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
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
    private int connectTimeout;
    private int readTimeout;

    private RetryTemplate retryTemplate;
    private RestTemplate restTemplate;

    @PostConstruct
    private void init(){

        fieldsToBioLark = Arrays.asList(env.getProperty("fieldsToBioLark").toLowerCase().split(","));
        endPoint = env.getProperty("biolarkEndPoint");
        setFieldName(env.getProperty("biolarkFieldName"));
        this.mapper = new ObjectMapper();
        this.connectTimeout = Integer.valueOf(env.getProperty("biolarkConnectTimeout"));
        this.readTimeout = Integer.valueOf(env.getProperty("biolarkReadTimeout"));
        this.retryTemplate = getRetryTemplate();
        this.restTemplate = new RestTemplate();
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(connectTimeout);
    }

    private List<String> fieldsToBioLark;

    public void setFieldsToBioLark(List<String> fields){
        this.fieldsToBioLark = fields;
    }


    @Override
    public Document process(final Document doc)  {
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());

        executeWithRetryIgnoringExceptions(doc);
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    private RetryTemplate getRetryTemplate(){
        TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
        retryPolicy.setTimeout(Long.valueOf(env.getProperty("biolarkRetryTimeout")));
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(Long.valueOf(env.getProperty("biolarkRetryBackoff")));

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }


    //not currently used but migh go back to this method
    private Document  executeWithRetryThrowingExceptions(Document doc){
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(doc.getAssociativeArray());
        doc.getAssociativeArray().forEach((k, v)-> {
            if (fieldsToBioLark.contains(k.toLowerCase())) {
                Object json = retryTemplate.execute(new RetryCallback<Object,BiolarkProcessingFailedException>() {
                    public Object doWithRetry(RetryContext context) {
                        // business logic here
                        return restTemplate.postForObject(endPoint, v, Object.class);
                    }
                }, new RecoveryCallback() {
                    @Override
                    public Object recover(RetryContext context) throws BiolarkProcessingFailedException {
                        LOG.warn("Biolark failed on document "+ doc.getDocName());
                        throw new  BiolarkProcessingFailedException("Biolark failed on document "+ doc.getDocName(),context.getLastThrowable());
                    }
                });
                newMap.put(fieldName,json);
            }
        });
        doc.getAssociativeArray().clear();
        doc.getAssociativeArray().putAll(newMap);
        return doc;
    }

    private Document  executeWithRetryIgnoringExceptions(Document doc){
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(doc.getAssociativeArray());
        doc.getAssociativeArray().forEach((k, v)-> {
            if (fieldsToBioLark.contains(k.toLowerCase())) {
                Object json = retryTemplate.execute(new RetryCallback<Object,BiolarkProcessingFailedException>() {
                    public Object doWithRetry(RetryContext context) {
                        // business logic here
                        return restTemplate.postForObject(endPoint, v, Object.class);
                    }
                }, new RecoveryCallback() {
                    @Override
                    public Object recover(RetryContext context) throws BiolarkProcessingFailedException {
                        LOG.warn("Biolark failed on document "+ doc.getDocName());
                        ArrayList<LinkedHashMap<Object,Object>> al = new ArrayList<LinkedHashMap<Object, Object>>();
                        LinkedHashMap<Object,Object> hm = new LinkedHashMap<Object, Object>();
                        hm.put(fieldName,"biolark failed");
                        al.add(hm);
                        doc.getExceptions().add(new BiolarkProcessingFailedException("Biolark failed on document "+ doc.getDocName()));
                        return al;
                    }
                });
                newMap.put(fieldName,json);
            }
        });
        doc.getAssociativeArray().clear();
        doc.getAssociativeArray().putAll(newMap);
        return doc;
    }

}
