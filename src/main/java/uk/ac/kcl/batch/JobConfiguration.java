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

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author rich
 */
@Configuration
@PropertySource("file:${gate_cog}")
@EnableBatchProcessing
@ComponentScan(basePackages = {"uk.ac.kcl.batch"})
public class JobConfiguration {
  
//    @Autowired
//    Environment env;
//
//        
//    @Bean
//    public ItemReader<BinaryDocument> reader() throws Exception {
//        //swapped for threadsafety
//        //SynchronizedItemStreamReader<BinaryDocument> reader2 = new SynchronizedItemStreamReader<>();
//        
////is bust for postgre driver? cursor not working properly?
////       JdbcCursorItemReader<BinaryDocument> reader = new JdbcCursorItemReader<>();
////        reader.setFetchSize(Integer.parseInt(env.getProperty("fetchSize")));
////        reader.setSql(env.getProperty("source.Sql"));
////        reader.setDataSource(jdbcDocumentSource.getJdbcTemplate().getDataSource());        
//        
//        JdbcPagingItemReader<BinaryDocument> reader = new JdbcPagingItemReader<>();
//        reader.setDataSource(jdbcDocumentSource.getJdbcTemplate().getDataSource()); 
//        
//        SqlPagingQueryProviderFactoryBean qp =  new SqlPagingQueryProviderFactoryBean();
//        qp.setSelectClause(env.getProperty("source.selectClause"));
//        qp.setFromClause(env.getProperty("source.fromClause"));       
//        qp.setSortKey(env.getProperty("source.sortKey"));    
//        qp.setWhereClause(env.getProperty("source.whereClause"));
//        qp.setDataSource(jdbcDocumentSource.getJdbcTemplate().getDataSource());
//        reader.setFetchSize(Integer.parseInt(env.getProperty("source.pageSize")));
//
//        reader.setQueryProvider(qp.getObject());        
//        reader.setRowMapper(documentRowMapper);
//        return reader;
//    }    
//    
//    @Bean
//    public ItemProcessor<BinaryDocument, BinaryDocument> gateDocumentItemProcessor() {
//        return new GateDocumentItemProcessor();
//    }
//    @Bean(initMethod = "init")
//    public GateService gateService(){        
//        return new GateService(
//                new File(env.getProperty("gateHome"))
//                ,new File(env.getProperty("gateApp"))
//                ,Integer.parseInt(env.getProperty("poolSize"))
//                , Arrays.asList(env.getProperty("gateAnnotationSets").split(",")));
//                
//    }         
//
//    @Bean
//    public ItemWriter<BinaryDocument> writer() {
//        JdbcBatchItemWriter<BinaryDocument> writer = new JdbcBatchItemWriter<>();
//        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
//        writer.setSql(env.getProperty("target.Sql"));
//        writer.setDataSource(jdbcDocumentTarget.getJdbcTemplate().getDataSource());
//        return writer;
//    }    
//    @Bean
//    @Qualifier("documentRowMapper")
//    public RowMapper documentRowMapper(){
//        DocumentMetadataRowMapper<BinaryDocument> documentMetadataRowMapper = new DocumentMetadataRowMapper<>();
//        List<String> otherFields = Arrays.asList(env.getProperty("otherFieldsList").split(","));
//        documentMetadataRowMapper.setOtherFieldsList(otherFields);
//        return documentMetadataRowMapper;
//    }            
//    
//    @Bean
//    @Qualifier("validationQueryRowMapper")
//    public RowMapper validationQueryRowMapper(){
//        DocumentMetadataRowMapper<BinaryDocument> documentMetadataRowMapper = new DocumentMetadataRowMapper<>();
//        List<String> otherFields = Arrays.asList(env.getProperty("target.validationQueryFields").split(","));
//        documentMetadataRowMapper.setOtherFieldsList(otherFields);
//        return documentMetadataRowMapper;
//    }
//
//    
//    @Bean
//    public JobCompleteNotificationListener listener (){
//        return new JobCompleteNotificationListener();
//    };  
//    @Bean
//    public CustomItemReaderListener customItemReaderListener(){
//        return new CustomItemReaderListener();
//    }
//    @Bean
//    public CustomItemWriterListener customItemWriterListener(){
//        return new CustomItemWriterListener();
//    }
//    @Bean
//    public GateStepListener customStepListener(){
//        return new GateStepListener();
//    }    
//            
//    
//    @Autowired
//    RowMapper<BinaryDocument> documentRowMapper;
//    @Autowired
//    JDBCDocumentTarget jdbcDocumentTarget;
//    @Autowired
//    JDBCDocumentSource jdbcDocumentSource;  
//    
//     
//    @Autowired
//    public ItemReader<BinaryDocument> itemreader;
//    
//    @Autowired
//    public ItemWriter<BinaryDocument> itemWriter;
//        
////    @Autowired
////    public StepBuilderFactory stepBuilderFactory;    
//    
//    @Autowired
//    public JobCompleteNotificationListener jobDoneListener;    
//
//    @Autowired
//    public CustomItemReaderListener readerListener;
//    
//    @Autowired
//    public CustomItemWriterListener writerListener;
//            
//    @Autowired
//    public GateStepListener stepListener;
//    
//    @Autowired
//    public TaskExecutor taskExecutor;
//    
//    @Bean
//    public TaskExecutor taskExecutor(){
//        SimpleAsyncTaskExecutor exec = new SimpleAsyncTaskExecutor();
//        //exec.setConcurrencyLimit(Integer.parseInt(env.getProperty("concurrencyLimit")));
//        return exec;
//    }
//    
//    @Bean
//    public Job importUserJob(JobBuilderFactory jobs, Step s1, JobExecutionListener listener) {
//        return jobs.get("importUserJob")
//                .incrementer(new RunIdIncrementer())
//                .listener(jobDoneListener)
//                .flow(s1)
//                .end()
//                .build();
//    }
//
//    @Bean
//    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<BinaryDocument> reader,
//            ItemWriter<BinaryDocument> writer,    ItemProcessor<BinaryDocument, BinaryDocument> processor) {
//        return stepBuilderFactory.get("step1")
//                .<BinaryDocument, BinaryDocument> chunk(Integer.parseInt(env.getProperty("chunkSize")))
//                .reader(reader).listener(readerListener)
//                .processor(processor)
//                .writer(writer).listener(writerListener)                 
//                .listener(stepListener)
//                .taskExecutor(taskExecutor)
//                .throttleLimit(Integer.parseInt(env.getProperty("throttleLimit")))
//                .build();
//    }            
//        
//      
//    
//    
  
    
}
