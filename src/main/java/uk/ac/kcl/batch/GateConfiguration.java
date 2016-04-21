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
import uk.ac.kcl.itemProcessors.GateDocumentItemProcessor;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.TextDocument;
import uk.ac.kcl.service.GateService;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Import(ItemHandlers.class)
@Configuration
@Profile("gate")
@PropertySource("file:${TURBO_LASER}/gate.conf")
public class GateConfiguration {

    @Resource
    Environment env;

    /*
    *******************************************GATE JOB
     */




    @Bean
    @Qualifier("gateItemProcessor")
    public ItemProcessor<TextDocument, TextDocument> gateDocumentItemProcessor() {
        return new GateDocumentItemProcessor();
    }

    @Bean(initMethod = "init")
    public GateService gateService() {
        if (env.getProperty("gateHome") != null) {
            return new GateService(
                    new File(env.getProperty("gateHome")),
                    new File(env.getProperty("gateApp")),
                    Integer.parseInt(env.getProperty("poolSize")),
                    Arrays.asList(env.getProperty("gateAnnotationSets").split(",")));
        } else {
            return new GateService();
        }
    }



    @Bean
    public Step gateSlaveStep(
            @Qualifier("textDocumentItemReader") ItemReader<TextDocument> reader,
            @Qualifier("compositeESandJdbcItemWriter") ItemWriter<Document> writer,
            @Qualifier("gateItemProcessor") ItemProcessor<TextDocument, TextDocument> processor,
            StepBuilderFactory stepBuilderFactory,
            @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor
    ) {
        Step step = stepBuilderFactory.get("gateSlaveStep")
                .<TextDocument, TextDocument>chunk(Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(Integer.valueOf(env.getProperty("skipLimit")))
                .skip(Exception.class)
                .taskExecutor(taskExecutor)
                .build();

        return step;
    }
}
