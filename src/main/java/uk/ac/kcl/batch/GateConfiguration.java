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
import org.springframework.dao.TransientDataAccessResourceException;
import uk.ac.kcl.itemHandlers.ItemHandlers;
import uk.ac.kcl.itemProcessors.DeIdDocumentItemProcessor;
import uk.ac.kcl.itemProcessors.GateDocumentItemProcessor;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.TextDocument;
import uk.ac.kcl.service.GateService;

import javax.annotation.Resource;
import java.io.File;
import java.net.NoRouteToHostException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Import(ItemHandlers.class)
@Configuration
@ComponentScan("uk.ac.kcl.service")
@PropertySource(value="file:${TURBO_LASER}/gateJob.conf" , ignoreResourceNotFound = true)
public class GateConfiguration {

    @Resource
    Environment env;

    /*
    *******************************************GATE JOB
     */




    @Bean
    @Profile("gate")
    @Qualifier("gateItemProcessor")
    public ItemProcessor<Document, Document> gateDocumentItemProcessor() {
        GateDocumentItemProcessor proc = new  GateDocumentItemProcessor();
        proc.setFieldName(env.getProperty("gateFieldName"));
        return proc;
    }



    @Bean
    @Profile("deid")
    @Qualifier("deIdDocumentItemProcessor")
    public ItemProcessor<Document,Document> deIdDocumentItemProcessor(){
        DeIdDocumentItemProcessor processor = new DeIdDocumentItemProcessor();
        processor.setFieldsToDeId(Arrays.asList(env.getProperty("fieldsToDeId").split(",")));
        return processor;
    }


    @Bean
    @Profile("gate")
    public Step gateSlaveStep(
            @Qualifier("textDocumentItemReader") ItemReader<TextDocument> reader,
            @Qualifier("compositeESandJdbcItemWriter") ItemWriter<Document> writer,
            @Qualifier("compositeItemProcessor") ItemProcessor<Document, Document> processor,
            StepBuilderFactory stepBuilderFactory,
            @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor
    ) {
        Step step = stepBuilderFactory.get("gateSlaveStep")
                .<Document, Document>chunk(Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(Integer.valueOf(env.getProperty("skipLimit")))
                .noSkip(Exception.class)
                //add acceptable exceptions here
                .taskExecutor(taskExecutor)
                .build();

        return step;
    }
}
