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

package uk.ac.kcl.batch;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import uk.ac.kcl.itemHandlers.ItemHandlers;
import uk.ac.kcl.itemProcessors.DbLineFixerItemProcessor;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.model.Document;

import javax.annotation.Resource;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */

@Import(ItemHandlers.class)
@Profile("basic")
@PropertySource("file:${TURBO_LASER}/basic.conf")
@Configuration
public class BasicJobConfiguration {
    @Resource
    Environment env;
    /* 
    
    
    
    *******************************************Basic Job
    
*/




    @Bean
    public Step basicSlaveStep(
            @Qualifier("documentItemReader") ItemReader<Document> reader,
            @Qualifier("compositeESandJdbcItemWriter")  ItemWriter<Document> writer,
            StepBuilderFactory stepBuilderFactory
    ) {
        Step step = stepBuilderFactory.get("dBLineFixerSlaveStep")
                .<Document, Document> chunk(
                        Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skipLimit(Integer.parseInt(env.getProperty("skipLimit")))
                .skip(Exception.class)
                .build();

        return step;
    }
}
