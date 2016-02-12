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

import uk.ac.kcl.batch.BatchConfigurer;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.ItemProcessors.GateDocumentItemProcessor;
import uk.ac.kcl.exception.GateProcessingFailedException;
//import io.bluecell.data.JDBCDocumentSource;
//import io.bluecell.data.JDBCDocumentTarget;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.rowmappers.DocumentMetadataRowMapper;
import uk.ac.kcl.service.GateService;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
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
import org.springframework.batch.sample.common.ColumnRangePartitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author rich
 */

@EnableIntegration
@Configuration
@PropertySource("classpath:test_config.properties")
@EnableBatchProcessing
@ImportResource("classpath:spring.xml")
@ComponentScan(basePackages = {"uk.ac.kcl.batch"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = JobConfiguration.class),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BatchConfigurer.class)})
public class TestJobConfiguration {
    /* 
        
    
    *******************************************COMMON BEANS
        
    
    */
    
//    
//    @Autowired
//    public StepBuilderFactory stepBuilderFactory;    
//    @Autowired
//    public JobCompleteNotificationListener jobDoneListener;    
    @Resource
    public Environment env;                    
    @Autowired
    public TaskExecutor taskExecutor;

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor exec = new SimpleAsyncTaskExecutor();
        //SyncTaskExecutor exec = new SyncTaskExecutor();
        //exec.setConcurrencyLimit(Integer.parseInt(env.getProperty("concurrencyLimit")));
        return exec;
    }    
    
    @Bean
    Partitioner partitioner(@Qualifier("sourceDataSource") DataSource jdbcDocumentSource) {
        ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
        columnRangePartitioner.setColumn(env.getProperty("columntoPartition"));
        columnRangePartitioner.setTable(env.getProperty("tableToPartition"));
        columnRangePartitioner.setDataSource(jdbcDocumentSource);
        return columnRangePartitioner;
    }


    
//    @Autowired
//    public Partitioner partitioner;
    
    @Bean
    Step masterStep(StepBuilderFactory stepBuilderFactory, 
            Partitioner partitioner, 
            @Qualifier("partitionHandler") PartitionHandler gatePartitionHandler) {
        return stepBuilderFactory.get("masterStep")
                .partitioner("masterStep", partitioner)
                .partitionHandler(gatePartitionHandler)
                .taskExecutor(taskExecutor)
                .build();
    }
    
    @Bean
    @Qualifier("validationQueryRowMapper")
    public RowMapper<BinaryDocument> validationQueryRowMapper() {
        DocumentMetadataRowMapper<BinaryDocument> documentMetadataRowMapper = new DocumentMetadataRowMapper<>();
        List<String> otherFields = Arrays.asList(env.getProperty("target.validationQueryFields").split(","));
        documentMetadataRowMapper.setOtherFieldsList(otherFields);
        return documentMetadataRowMapper;
    }

//    @Bean
//    public JobCompleteNotificationListener listener() {
//        return new JobCompleteNotificationListener();
//    }    
 
    /* 
    
    
    
    *******************************************GATE JOB
    
    
    
    */
    
    
    
    
    @Bean
    @StepScope
    @Qualifier("gateItemReader")
    public ItemReader<BinaryDocument> reader(
            @Qualifier("documentRowMapper")RowMapper<BinaryDocument> documentRowmapper, 
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {
        //swapped for threadsafety
        //SynchronizedItemStreamReader<BinaryDocument> reader2 = new SynchronizedItemStreamReader<>();
//        JdbcCursorItemReader<BinaryDocument> reader = new JdbcCursorItemReader<>();
//        reader.setFetchSize(Integer.parseInt(env.getProperty("fetchSize")));
//        reader.setMaxRows(Integer.parseInt(env.getProperty("maxRows")));         
//        reader.setDataSource(jdbcDocumentSource.getJdbcTemplate().getDataSource());
//        reader.setSql(env.getProperty("source.Sql"));

        JdbcPagingItemReader<BinaryDocument> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(jdbcDocumentSource);

        SqlPagingQueryProviderFactoryBean qp = new SqlPagingQueryProviderFactoryBean();
        qp.setSelectClause(env.getProperty("source.selectClause"));
        qp.setFromClause(env.getProperty("source.fromClause"));
        qp.setSortKey(env.getProperty("source.sortKey"));
        qp.setWhereClause(env.getProperty("source.whereClause"));
        qp.setDataSource(jdbcDocumentSource);
        reader.setFetchSize(Integer.parseInt(env.getProperty("source.pageSize")));

        reader.setQueryProvider(qp.getObject());
        reader.setRowMapper(documentRowmapper);

        //reader2.setDelegate(reader);
        return reader;
    }

    @Bean
    @Qualifier("gateItemProcessor")
    public ItemProcessor<BinaryDocument, BinaryDocument> gateDocumentItemProcessor() {
        return new GateDocumentItemProcessor();
    }

    @Bean(initMethod = "init")
    public GateService gateService() {
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
    @Qualifier("documentRowMapper")
    public RowMapper documentRowMapper() {
        DocumentMetadataRowMapper<BinaryDocument> documentMetadataRowMapper = new DocumentMetadataRowMapper<>();
        List<String> otherFields = Arrays.asList(env.getProperty("otherFieldsList").split(","));
        documentMetadataRowMapper.setOtherFieldsList(otherFields);
        return documentMetadataRowMapper;
    }




//    @Autowired
//    RowMapper<BinaryDocument> documentRowMapper;
//    @Autowired
//    @Qualifier("targetDataSource")
//    public DataSource jdbcDocumentTarget;
//    @Autowired
//    @Qualifier("sourceDataSource")    
//    public DataSource jdbcDocumentSource;

    @Bean
    public Step gateSlaveStep(    
            @Qualifier("gateItemReader")ItemReader<BinaryDocument> reader,
            @Qualifier("gateItemWriter")  ItemWriter<BinaryDocument> writer,    
            @Qualifier("gateItemProcessor")   ItemProcessor<BinaryDocument, BinaryDocument> processor,
            StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("gateSlaveStep")
                .<BinaryDocument, BinaryDocument> chunk(Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(GateProcessingFailedException.class)   
                .taskExecutor(taskExecutor)
                .build();
    }       
    
    @Bean
    public Job gateJob(JobBuilderFactory jobs,  
            StepBuilderFactory stepBuilderFactory,
            Partitioner partitioner, 
            @Qualifier("partitionHandler") PartitionHandler gatePartitionHandler) {
        return jobs.get("gateJob")
                .incrementer(new RunIdIncrementer())
                .flow(masterStep(stepBuilderFactory, partitioner,gatePartitionHandler))
                .end()
                .build();
    }

    
    
    @Bean(destroyMethod = "close")
    @Primary
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(env.getProperty("source.Driver"));
        ds.setUrl(env.getProperty("source.JdbcPath"));
        ds.setUsername(env.getProperty("source.username"));
        ds.setPassword(env.getProperty("source.password"));        
        return ds;
    }

    @Bean(destroyMethod = "close")
    @Qualifier("targetDataSource")
    public DataSource targetDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(env.getProperty("target.Driver"));
        ds.setUrl(env.getProperty("target.JdbcPath"));
        ds.setUsername(env.getProperty("target.username"));
        ds.setPassword(env.getProperty("target.password"));                
        return ds;
    }    
    /* 
    
    
    
    *******************************************Multiline JOB
    
    
    
    */
        


     

    
    

}
