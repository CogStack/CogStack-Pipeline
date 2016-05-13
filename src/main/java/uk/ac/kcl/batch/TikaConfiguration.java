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
import uk.ac.kcl.itemProcessors.TikaDocumentItemProcessor;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.model.Document;

import javax.annotation.Resource;
import java.net.NoRouteToHostException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Import(ItemHandlers.class)
@Configuration
@Profile("tika")
@PropertySource(value="file:${TURBO_LASER}/tikaJob.conf", ignoreResourceNotFound = true)
public class TikaConfiguration {
    @Resource
    Environment env;

    /* 
    
    
    
    *******************************************Tika JOB
    
    
    
    */
    


    
    @Bean
    @Qualifier("tikaItemProcessor")
    public ItemProcessor<BinaryDocument, BinaryDocument> tikaDocumentItemProcessor() {
        TikaDocumentItemProcessor proc = new TikaDocumentItemProcessor();
        proc.setKeepTags(Boolean.valueOf(env.getProperty("keepTags")));
        return new TikaDocumentItemProcessor();
    }


    @Bean
    public Step tikaSlaveStep(    
            @Qualifier("binaryDocumentItemReader")ItemReader<BinaryDocument> reader,
            @Qualifier("compositeESandJdbcItemWriter")  ItemWriter<Document> writer,
            @Qualifier("tikaItemProcessor")   ItemProcessor<BinaryDocument, BinaryDocument> processor,
            @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor,
            StepBuilderFactory stepBuilderFactory
            ) {
         Step step = stepBuilderFactory.get("tikaSlaveStep")
                .<BinaryDocument, BinaryDocument> chunk(Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .processor(processor)                 
                .writer(writer)
                .faultTolerant()
                .skipLimit(Integer.valueOf(env.getProperty("skipLimit")))
                .skip(Exception.class)
                 .noSkip(SQLException.class)
                 .noSkip(TimeoutException.class)
                 .noSkip(NoRouteToHostException.class)
                 .noSkip(TransientDataAccessResourceException.class)
                .taskExecutor(taskExecutor)                 
                .build();
         return step;
    }
}
