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
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.exception.DeIdentificationFailedException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author rich
 */



@Service("gateService")
@Profile({"gate"})
public class GateService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GateService.class);


    private LinkedBlockingQueue<CorpusController> genericQueue;
    private int poolSize;
    private Iterable<String> annotationSets;

    private LinkedBlockingQueue<CorpusController> deIdQueue;


    @Autowired
    private Environment env;

    private GateService() {
    }

    @PostConstruct
    public void init() throws GateException, IOException {

        File gateHome = new File(env.getProperty("gateHome"));
        poolSize = Integer.parseInt(env.getProperty("poolSize"));
        //in case called by other contexts
        if(!Gate.isInitialised()) {
            Gate.setGateHome(gateHome);
            Gate.runInSandbox(true);
            Gate.init();
        }

        loadresources();
    }

    private void loadresources() throws GateException, IOException {
        List<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        //if called assume resources are either new/damaged/stale etc and delete all
        Gate.getCreoleRegister().getAllInstances("gate.Resource").forEach(Factory::deleteResource);

        if(activeProfiles.contains("gate")){
            File gateApp = new File(env.getProperty("gateApp"));
            annotationSets = Arrays.asList(env.getProperty("gateAnnotationSets").split(","));
            genericQueue = new LinkedBlockingQueue<>();
            Corpus corpus = Factory.newCorpus("Corpus");
            CorpusController pipeline = (CorpusController) PersistenceManager
                    .loadObjectFromFile(gateApp);
            pipeline.setCorpus(corpus);
            genericQueue.add(pipeline);
            while (genericQueue.size() != poolSize) {
                genericQueue.add((CorpusController) Factory.duplicate(pipeline));
            }
        }


        if(activeProfiles.contains("deid")){
            File deidApp = new File(env.getProperty("deIdApp"));
            deIdQueue = new LinkedBlockingQueue<>();
            Corpus corpus = Factory.newCorpus("Corpus");
            CorpusController pipeline = (CorpusController) PersistenceManager
                    .loadObjectFromFile(deidApp);
            pipeline.setCorpus(corpus);
            deIdQueue.add(pipeline);
            while (deIdQueue.size() != poolSize) {
                deIdQueue.add((CorpusController) Factory.duplicate(pipeline));
            }
        }
    }

    public void processDoc(Document doc) throws ExecutionException {
        CorpusController controller = null;
        try {
            controller = genericQueue.take();
        } catch (InterruptedException ex) {
            LOG.warn("GATE app execution interrupted", ex);
        }
        assert controller != null;
        controller.getCorpus().add(doc);
        controller.execute();
        controller.getCorpus().clear();
        try {
            genericQueue.put(controller);
        } catch (InterruptedException ex) {
            LOG.info("Interrupted", ex);
        }
    }

    public String deIdentifyString(String text, String primaryKeyFieldValue) throws DeIdentificationFailedException {
        Document doc;
        try {
            doc = Factory.newDocument(text);

            doc.getFeatures().put("primaryKeyFieldValue", primaryKeyFieldValue);
            CorpusController controller;

            controller = deIdQueue.take();

            controller.getCorpus().add(doc);
            controller.execute();
            controller.getCorpus().clear();

            deIdQueue.put(controller);

            text = doc.getContent().toString();
            Factory.deleteResource(doc);
        }catch (Exception ex) {
            LOG.error("GATE app execution error", ex);
            try {
                loadresources();
            } catch (GateException|IOException e) {
                LOG.error("could not reload resources", ex);
            }
            throw new DeIdentificationFailedException("GATE app execution error");
        }
        return text;
    }


    public String convertDocToJSON(gate.Document doc) throws IOException {
        Map<String, Collection<Annotation>> map = new HashMap<>();
        //code to retrive specific annotation sets. revisit later        
        for (String ASName : annotationSets) {
            if (ASName != null) {
                map.put(ASName, doc.getAnnotations(ASName));
            } else {
                map.put("", doc.getAnnotations(null));
            }
        }
        return gate.corpora.DocumentJsonUtils.toJson(doc, map);
    }
}
