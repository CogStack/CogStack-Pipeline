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


import javax.sql.DataSource;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import uk.ac.kcl.partitioners.ColumnRangePartitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

/**
 *
 * @author rich
 */

@EnableIntegration
@Configuration
@EnableBatchProcessing
@Import({          
            DbLineFixerConfiguration.class, 
            GateConfiguration.class,
            BatchConfigurer.class,
            TikaConfiguration.class,
            SlaveIntegrationConfiguration.class,
            MasterIntegrationConfiguration.class
            })
public class JobConfiguration {
    /* 
        
    
    *******************************************COMMON BEANS
        
    
    */
    //required to process placeholder values in annotations, e.g. scheduler cron
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        PropertySourcesPlaceholderConfigurer props = new PropertySourcesPlaceholderConfigurer();
        props.setNullValue("null");
        return new PropertySourcesPlaceholderConfigurer();
    }     
    @Autowired
    public Environment env;      
    
    
    

    @Bean
    @Qualifier("slaveTaskExecutor")
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor exec = new SimpleAsyncTaskExecutor();
        exec.setConcurrencyLimit(Integer.parseInt(env.getProperty("concurrencyLimit")));
        return exec;
    }    

    @Bean
    public Partitioner partitioner(@Qualifier("sourceDataSource") DataSource jdbcDocumentSource) {
        ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
        columnRangePartitioner.setColumn(env.getProperty("columntoPartition"));
        columnRangePartitioner.setTable(env.getProperty("tableToPartition"));
        columnRangePartitioner.setDataSource(jdbcDocumentSource);
        return columnRangePartitioner;
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

    
     
    
  
//    
            
    @Bean
    public BeanFactoryStepLocator stepLocator(){
        return new BeanFactoryStepLocator();
    }
    
    @Bean 
    public CachingConnectionFactory connectionFactory(ActiveMQConnectionFactory factory){
        return new CachingConnectionFactory(factory);
    }

    @Bean
    public ActiveMQConnectionFactory amqConnectionFactory(){
        ActiveMQConnectionFactory factory = 
                new ActiveMQConnectionFactory(env.getProperty("jmsIP"));
        factory.setUserName(env.getProperty("jmsUsername"));
        factory.setPassword(env.getProperty("jmsPassword"));
        return factory;
    }
          
   
    
    @Bean
    public StepExecutionRequestHandler stepExecutionRequestHandler(
    JobExplorer jobExplorer, BeanFactoryStepLocator stepLocator){
        StepExecutionRequestHandler handler = new StepExecutionRequestHandler();
        handler.setJobExplorer(jobExplorer);
        handler.setStepLocator(stepLocator);
        return handler;
    }
}
