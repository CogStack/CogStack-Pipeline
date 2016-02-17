/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.kcl.batch;

import gate.util.GateException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.config.EnableIntegration;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.model.SimpleDocument;

/**
 *
 * @author kcladmin
 */
@PropertySource("classpath:test_config.properties")
@ComponentScan(basePackages = {"uk.ac.kcl.batch"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = JobConfiguration.class),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BatchConfigurer.class)})
public class TestJobs {
    
    @Autowired
    Environment env;
    
    @Bean
    public Job gateJob(JobBuilderFactory jobs, 
            StepBuilderFactory steps,
            Partitioner partitioner, 
            @Qualifier("partitionHandler") 
                    PartitionHandler gatePartitionHandler,
                    TaskExecutor taskExecutor){
                Job job = jobs.get("gateJob")
                        .incrementer(new RunIdIncrementer())
                        .flow(
                                steps
                                        .get("gateMasterStep")
                                        .partitioner("gateSlaveStep", partitioner)
                                        .partitionHandler(gatePartitionHandler)
                                        .taskExecutor(taskExecutor)
                                        .build()
                                
                        )
                        .end()
                        .build();
                return job;
                        
    }
    

    
    @Bean
    public Step gateSlaveStep(    
            @Qualifier("gateItemReader")ItemReader<BinaryDocument> reader,
            @Qualifier("gateItemWriter")  ItemWriter<BinaryDocument> writer,    
            @Qualifier("gateItemProcessor")   ItemProcessor<BinaryDocument, BinaryDocument> processor,
            StepBuilderFactory stepBuilderFactory
    //        @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor
            ) {
         Step step = stepBuilderFactory.get("gateSlaveStep")
                .<BinaryDocument, BinaryDocument> chunk(Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(GateException.class)   
                //.taskExecutor(taskExecutor)
                .build();
         
         return step;
    }       
    
    @Bean
    public Job dbLineFixerJob(JobBuilderFactory jobs, 
            StepBuilderFactory steps,
            Partitioner partitioner, 
            @Qualifier("partitionHandler") 
                    PartitionHandler gatePartitionHandler,
                    TaskExecutor taskExecutor){
                Job job = jobs.get("dbLineFixerJob")
                        .incrementer(new RunIdIncrementer())
                        .flow(
                                steps
                                        .get("dbLineFixerMasterStep")
                                        .partitioner("dbLineFixerSlaveStep", partitioner)
                                        .partitionHandler(gatePartitionHandler)
                                        .taskExecutor(taskExecutor)
                                        .build()
                                
                        )
                        .end()
                        .build();
                return job;
                        
    }
    

    
    @Bean
    public Step dbLineFixerSlaveStep(    
            @Qualifier("dBLineFixerItemReader") ItemReader<SimpleDocument> reader,
            @Qualifier("dBLineFixerItemWriter")  ItemWriter<SimpleDocument> writer,    
            StepBuilderFactory stepBuilderFactory
    //        @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor
            ) {
         Step step = stepBuilderFactory.get("dbLineFixerSlaveStep")
                .<SimpleDocument, SimpleDocument> chunk(
                        Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(GateException.class)   
                //.taskExecutor(taskExecutor)
                .build();
         
         return step;
    }      
}
