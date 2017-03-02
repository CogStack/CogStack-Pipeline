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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.kcl.exception.WebserviceProcessingFailedException;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import java.util.*;

@Profile("webservice")
@Service("webserviceDocumentItemProcessor")
@ComponentScan("uk.ac.kcl.service")
public class WebserviceDocumentItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebserviceDocumentItemProcessor.class);

    @Autowired
    private Environment env;
    @Value("${webservice.endPoint}")
    private String endPoint;
    @Value("${webservice.fieldName}")
    private String fieldName;
    @Value("${webservice.connectTimeout}")
    private int connectTimeout;
    @Value("${webservice.readTimeout}")
    private int readTimeout;
    @Value("${webservice.name}")
    private String webserviceName;

    private RetryTemplate retryTemplate;
    private RestTemplate restTemplate;
    private List<String> fieldsToSendToWebservice;

    @PostConstruct
    private void init(){

        fieldsToSendToWebservice = Arrays.asList(env.getProperty("webservice.fieldsToSendToWebservice")
                .toLowerCase().split(","));
        setFieldName(fieldName);
        this.retryTemplate = getRetryTemplate();
        this.restTemplate = new RestTemplate();
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(connectTimeout);
    }

    public void setFieldsToSendToWebservice(List<String> fields){
        this.fieldsToSendToWebservice = fields;
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
        retryPolicy.setTimeout(Long.valueOf(env.getProperty("webservice.retryTimeout")));
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(Long.valueOf(env.getProperty("webservice.retryBackoff")));

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
            if (fieldsToSendToWebservice.contains(k.toLowerCase())) {
                Object json = retryTemplate.execute(new RetryCallback<Object,WebserviceProcessingFailedException>() {
                    public Object doWithRetry(RetryContext context) {
                        // business logic here
                        return restTemplate.postForObject(endPoint, v, Object.class);
                    }
                }, new RecoveryCallback() {
                    @Override
                    public Object recover(RetryContext context) throws WebserviceProcessingFailedException {
                        LOG.warn(webserviceName +" failed on document "+ doc.getDocName());
                        throw new WebserviceProcessingFailedException(webserviceName +"  failed on document "
                                + doc.getDocName(),context.getLastThrowable());
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
            if (fieldsToSendToWebservice.contains(k.toLowerCase())) {
                Object json = retryTemplate.execute(new RetryCallback<Object,WebserviceProcessingFailedException>() {
                    public Object doWithRetry(RetryContext context) {
                        // business logic here
                        Object ob = restTemplate.postForObject(endPoint, v, Object.class);

                        return ob;
                    }
                }, new RecoveryCallback() {
                    @Override
                    public Object recover(RetryContext context) throws WebserviceProcessingFailedException {
                        LOG.warn(webserviceName +" failed on document "+ doc.getDocName());
                        ArrayList<LinkedHashMap<Object,Object>> al = new ArrayList<LinkedHashMap<Object, Object>>();
                        LinkedHashMap<Object,Object> hm = new LinkedHashMap<Object, Object>();
                        hm.put(fieldName,webserviceName +" failed");
                        al.add(hm);
                        doc.getExceptions().add(new WebserviceProcessingFailedException(webserviceName
                                +" failed on document " + doc.getDocName()));
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
