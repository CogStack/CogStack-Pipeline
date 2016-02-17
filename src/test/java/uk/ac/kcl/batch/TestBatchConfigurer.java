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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author rich
 */
@Configuration
@PropertySource("classpath:test_config.properties")
@EnableBatchProcessing
@ComponentScan(basePackages = {"uk.ac.kcl.batch"},excludeFilters={
@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=JobConfiguration.class), 
@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=BatchConfigurer.class)}        )
public class TestBatchConfigurer extends DefaultBatchConfigurer {



    public DataSource jdbcDocumentTarget;

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(jdbcDocumentTarget);
        factory.setTransactionManager(getTransactionManager());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
    
//    @Autowired
//    public setDataSource(@Qualifier("targetDAtaSource") DataSource ds){
//        retrun
//    }

    @Override
    @Autowired
    public void setDataSource(@Qualifier("targetDataSource")DataSource dataSource) {
        if (dataSource != null) {
            super.setDataSource(dataSource);
        }
        this.jdbcDocumentTarget = dataSource;
    }

//    @Bean
//    public DataSource dataSource() {
//        return jdbcDocumentTarget;
//    }

    @Autowired
    public PlatformTransactionManager getTransactionManager(
            @Qualifier("targetDataSource")
            DataSource jdbcDocumentTarget) {
        DataSourceTransactionManager tx = new DataSourceTransactionManager();
        tx.setDataSource(jdbcDocumentTarget);
        return tx;
    }

    
    @Bean
    public JobRegistry jobregistry(){
        return new MapJobRegistry();
    }
    
    @Bean
    @Autowired
    public JobExplorer jobExplorer(@Qualifier("targetDataSource")DataSource dataSource) throws Exception{
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        
        factory.afterPropertiesSet();
        return factory.getObject();
    }
    
    @Bean
    public JobLauncher getJobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        try {
            jobLauncher.afterPropertiesSet();
        } catch (Exception ex) {
            Logger.getLogger(TestBatchConfigurer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobLauncher;
    }
    
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }    
    
//    @Autowired
//    JobExplorer jobExplorer;
//    @Autowired
//    JobLauncher jobLauncher;
//    @Autowired
//    JobRegistry jobRegistry;
//    @Autowired
//    JobRepository jobRepository;
    
    @Bean
    public JobOperator jobOperator(
            JobExplorer jobExplorer, 
            JobLauncher jobLauncher, 
            JobRegistry jobRegistry, 
            JobRepository jobRepository) {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(jobExplorer);
        jobOperator.setJobLauncher(jobLauncher);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobRepository(jobRepository);
        return jobOperator;
    }

}
