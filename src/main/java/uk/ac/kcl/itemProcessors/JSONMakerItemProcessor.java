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

import com.google.gson.Gson;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentFactory.*;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import uk.ac.kcl.exception.TurboLaserException;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 *
 * @author rich
 *
 * a null item processor to meet composite requirements if no processing is required
 */
public class JSONMakerItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JSONMakerItemProcessor.class);

    @Autowired
    Environment env;
    private Boolean reindex;
    private String reindexField;

    @PostConstruct
    public void init(){
        this.reindex = Boolean.valueOf(env.getProperty("reindex"));
        if(reindex) {
            this.reindexField = env.getProperty("reindexField").toLowerCase();
        }
    }
    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        //may already be populated if reindexing
        //XContentBuilder builder = doc.getOutputData();
        if(!reindex) {
            XContentBuilder builder = jsonBuilder()
                    .map(doc.getAdditionalFields());
            doc.setOutputData(builder.string());
        }else{
            doc.setOutputData(doc.getAdditionalFields().get(reindexField).toString());
        }

        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }


}
