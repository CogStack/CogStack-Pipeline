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
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import uk.ac.kcl.model.Document;

/**
 *
 * @author rich
 *
 * a null item processor to meet composite requirements if no processing is required
 */
public class JSONMakerItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JSONMakerItemProcessor.class);

    @Override
    public Document process(final Document doc) throws Exception {
        Gson gson = new Gson();
        String json = gson.toJson(doc.getAdditionalFields());
        doc.setOutputData(json);

        return doc;
    }


}
