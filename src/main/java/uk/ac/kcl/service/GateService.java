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
package uk.ac.kcl.service;

import gate.*;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 *
 * @author rich
 */



@Service
@Profile({"gate","deid"})
public class GateService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GateService.class);


    File gateHome;
    File gateApp;
    LinkedBlockingQueue<CorpusController> genericQueue;
    private int poolSize;
    private Iterable<String> annotationSets;

    File deidApp;
    LinkedBlockingQueue<CorpusController> deIdQueue;


    @Autowired
    private Environment env;

    public GateService() {
    }

    @PostConstruct
    public void init() throws ResourceInstantiationException, GateException, PersistenceException, IOException {

        gateHome = new File(env.getProperty("gateHome"));
        poolSize = Integer.parseInt(env.getProperty("poolSize"));
        Gate.setGateHome(gateHome);
        Gate.init();
        List<String> activeProfiles = Arrays.asList(env.getActiveProfiles());

        if(activeProfiles.contains("gate")){
            gateApp = new File(env.getProperty("gateApp"));
            annotationSets = Arrays.asList(env.getProperty("gateAnnotationSets").split(","));
            genericQueue = new LinkedBlockingQueue<>();
            Corpus corpus = gate.Factory.newCorpus("Corpus");
            CorpusController pipeline = (CorpusController) PersistenceManager
                    .loadObjectFromFile(gateApp);
            pipeline.setCorpus(corpus);
            genericQueue.add(pipeline);
            while (genericQueue.size() != poolSize) {
                genericQueue.add((CorpusController) Factory.duplicate(pipeline));
            }
        }


        if(activeProfiles.contains("deid")){
            deidApp = new File(env.getProperty("deIdApp"));
            deIdQueue = new LinkedBlockingQueue<>();
            Corpus corpus = gate.Factory.newCorpus("Corpus");
            CorpusController pipeline = (CorpusController) PersistenceManager
                    .loadObjectFromFile(deidApp);
            pipeline.setCorpus(corpus);
            deIdQueue.add(pipeline);
            while (deIdQueue.size() != poolSize) {
                deIdQueue.add((CorpusController) Factory.duplicate(pipeline));
            }
        }
    }

    public gate.Document processDoc(gate.Document doc) throws ExecutionException {
        CorpusController controller = null;
        try {
            controller = genericQueue.take();
        } catch (InterruptedException ex) {
            LOG.warn("GATE app execution interrupted", ex);
        }
        controller.getCorpus().add(doc);
        controller.execute();
        controller.getCorpus().clear();
        try {
            genericQueue.put(controller);
        } catch (InterruptedException ex) {
            LOG.info("Interrupted", ex);
        }
        return doc;
    }

    public String deIdentifyString(String text, String primaryKeyFieldValue) throws ExecutionException, ResourceInstantiationException {
        gate.Document doc = Factory.newDocument(text);
        doc.getFeatures().put("primaryKeyFieldValue",primaryKeyFieldValue);
        CorpusController controller = null;
        try {
            controller = deIdQueue.take();
        } catch (InterruptedException ex) {
            LOG.warn("GATE app execution interrupted", ex);
        }
        controller.getCorpus().add(doc);
        controller.execute();
        controller.getCorpus().clear();
        try {
            deIdQueue.put(controller);
        } catch (InterruptedException ex) {
            LOG.info("Interrupted", ex);
        }
        text = doc.getContent().toString();
        Factory.deleteResource(doc);
        return text;
    }


    public String convertDocToJSON(gate.Document doc) throws IOException {
        Map<String, Collection<Annotation>> map = new HashMap<>();
        //code to retrive specific annotation sets. revisit later        
        for (String ASName : annotationSets) {
            if (ASName != null) {
                map.put(ASName, doc.getAnnotations(ASName));
            } else {
                map.put("", doc.getAnnotations(ASName));
            }
        }
        String json = gate.corpora.DocumentJsonUtils.toJson(doc, map);
        return json;
    }
}
