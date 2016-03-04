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

package uk.ac.kcl.batch.io;

import uk.ac.kcl.batch.*;
import gate.util.GateException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.kcl.ItemProcessors.GateDocumentItemProcessor;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.rowmappers.BinaryDocumentRowMapper;
import uk.ac.kcl.rowmappers.DocumentMetadataRowMapper;
import uk.ac.kcl.service.GateService;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Configuration
@Profile("tika")
public class TikaIOConfiguration {
    @Resource
    Environment env;

    /* 
    
    
    
    *******************************************GATE JOB
    
    
    
    */
    
    
    
    
    @Bean
    @StepScope
    @Qualifier("tikaItemReader")
    public ItemReader<BinaryDocument> reader(
            @Value("#{stepExecutionContext[minValue]}") String minValue,
            @Value("#{stepExecutionContext[maxValue]}") String maxValue,
            @Qualifier("simpleDocumentRowMapper")RowMapper<BinaryDocument> documentRowmapper, 
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {
        
        JdbcPagingItemReader<BinaryDocument> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(jdbcDocumentSource);

        SqlPagingQueryProviderFactoryBean qp = new SqlPagingQueryProviderFactoryBean();
        qp.setSelectClause(env.getProperty("source.selectClause"));
        qp.setFromClause(env.getProperty("source.fromClause"));
        qp.setSortKey(env.getProperty("source.sortKey"));
        //qp.setWhereClause(env.getProperty("source.whereClause"));
        qp.setWhereClause("WHERE " + env.getProperty("columntoPartition") + " BETWEEN " + minValue + " AND " + maxValue) ;
        qp.setDataSource(jdbcDocumentSource);
        reader.setFetchSize(Integer.parseInt(env.getProperty("source.pageSize")));

        
        reader.setQueryProvider(qp.getObject());
        reader.setRowMapper(documentRowmapper);

        //reader2.setDelegate(reader);
        return reader;
    }


    @Bean(initMethod = "init")
    public GateService gateService() {
        //if GateHome not set, assume running another type of job and return an empty pojo
        return new GateService(
                new File(env.getProperty("gateHome")), 
                new File(env.getProperty("gateApp")), 
                Integer.parseInt(env.getProperty("poolSize")), 
                Arrays.asList(env.getProperty("gateAnnotationSets").split(",")));

    }

    @Bean
    @Qualifier("gateItemWriter")
    public ItemWriter<BinaryDocument> writer(@Qualifier("targetDataSource") DataSource jdbcDocumentTarget) {
        JdbcBatchItemWriter<BinaryDocument> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(env.getProperty("target.Sql"));
        writer.setDataSource(jdbcDocumentTarget);
        return writer;
    }
    
    @Bean
    @Qualifier("tikaRowMapper")    
    public RowMapper documentRowMapper(){
        BinaryDocumentRowMapper rowMapper = new BinaryDocumentRowMapper();
        rowMapper.setBinaryFieldName(env.getProperty("binaryFieldName"));
        List<String> otherFields = Arrays.asList(env.getProperty("otherFieldsList").split(","));
        rowMapper.setOtherFieldsList(otherFields);
        return rowMapper;
    }        
    
    @Bean
    public DocumentMetadataRowMapper documentMetadataRowMapper(){
        DocumentMetadataRowMapper<BinaryDocument> documentMetadataRowMapper = new DocumentMetadataRowMapper<>();
        List<String> otherFields = Arrays.asList(env.getProperty("otherFieldsList").split(","));
        documentMetadataRowMapper.setOtherFieldsList(otherFields);
        return documentMetadataRowMapper;
    }    

    @Bean
    public Job tikaJob(JobBuilderFactory jobs, 
            StepBuilderFactory steps,
            Partitioner partitioner, 
            @Qualifier("partitionHandler") 
                    PartitionHandler gatePartitionHandler,
                    TaskExecutor taskExecutor){
                Job job = jobs.get("tikaJob")
                        .incrementer(new RunIdIncrementer())
                        .flow(
                                steps
                                        .get("tikaMasterStep")
                                        .partitioner("tikaSlaveStep", partitioner)
                                        .partitionHandler(gatePartitionHandler)
                                        .taskExecutor(taskExecutor)
                                        .build()
                                
                        )
                        .end()
                        .build();
                return job;
                        
    }
    

    
    @Bean
    public Step tikaSlaveStep(    
            @Qualifier("tikaItemReader")ItemReader<BinaryDocument> reader,
            @Qualifier("tikaItemWriter")  ItemWriter<BinaryDocument> writer,    
            @Qualifier("tikaItemProcessor")   ItemProcessor<BinaryDocument, BinaryDocument> processor,
            StepBuilderFactory stepBuilderFactory
    //        @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor
            ) {
         Step step = stepBuilderFactory.get("tikaSlaveStep")
                .<BinaryDocument, BinaryDocument> chunk(Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)   
                //.taskExecutor(taskExecutor)
                .build();
         
         return step;
    }           
    
    @Bean
    @Qualifier("validationQueryRowMapper")
    public RowMapper<BinaryDocument> validationQueryRowMapper() {
        DocumentMetadataRowMapper<BinaryDocument> documentMetadataRowMapper = new DocumentMetadataRowMapper<>();
        List<String> otherFields = Arrays.asList(env.getProperty("target.validationQueryFields").split(","));
        documentMetadataRowMapper.setOtherFieldsList(otherFields);
        return documentMetadataRowMapper;
    }
    
}
