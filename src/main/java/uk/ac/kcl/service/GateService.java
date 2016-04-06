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

import gate.Annotation;
import gate.Corpus;
import gate.CorpusController;
import gate.Factory;
import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author rich
 */
public class GateService {

    final static Logger logger = Logger.getLogger(GateService.class);


    File gateHome;
    File gateApp;
    LinkedBlockingQueue<CorpusController> queue;
    private int poolSize;
    private Iterable<String> annotationSets;

    public GateService() {
    }
    public GateService(File gateHome, 
            File gateApp, 
            int poolSize, 
            List<String> annotationSets) {
        this.gateApp = gateApp;
        this.gateHome = gateHome;
        this.poolSize = poolSize;
        this.annotationSets = annotationSets;
    }

    public void init() throws ResourceInstantiationException, GateException, PersistenceException, IOException {
        if(gateHome !=null){ 
            Gate.setGateHome(gateHome);
            Gate.init();
            queue = new LinkedBlockingQueue<>();
            Corpus corpus = gate.Factory.newCorpus("Corpus");
            CorpusController pipeline = (CorpusController) PersistenceManager
                    .loadObjectFromFile(gateApp);
            pipeline.setCorpus(corpus);
            queue.add(pipeline);
            while(queue.size() != poolSize) {
                queue.add((CorpusController) Factory.duplicate(pipeline));
            }
        }
    }

    public gate.Document processDoc(gate.Document doc) throws ExecutionException {
        CorpusController controller = null;
        try {
            controller = queue.take();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(GateService.class.getName()).log(Level.SEVERE, null, ex);
        }
        controller.getCorpus().add(doc);
        controller.execute();
        controller.getCorpus().clear();
        try {
            queue.put(controller);
        } catch (InterruptedException ex) {
            logger.info("Interrupted", ex);
        }
        return doc;
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
