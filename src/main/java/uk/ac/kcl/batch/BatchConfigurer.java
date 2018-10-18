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

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rich
 */
@Configuration
public class BatchConfigurer extends DefaultBatchConfigurer {

    @Autowired
    Environment env;

    // mandatory properties
    @Value("${jobRepository.Driver}")
    private String repoDriver;
    @Value("${jobRepository.JdbcPath}")
    private String repoJdbcPath;
    @Value("${jobRepository.username}")
    private String repoUserName;
    @Value("${jobRepository.JdbcPath}")
    private String repoPassword;

    // optional job repository DB properties with default values
    @Value("${jobRepository.idleTimeout:30000}")
    private Long repoIdleTimeoutMs;
    @Value("${jobRepository.maxLifetime:60000}")
    private Long repoMaxLifeTimeMs;
    @Value("${jobRepository.poolSize:10}")
    private int repoPoolSize;

    @Autowired
    @Qualifier("jobRepositoryDataSource")
    DataSource jobRepositoryDataSource;

    @Bean(destroyMethod = "close")
    @Qualifier("jobRepositoryDataSource")
    public DataSource jobRepositoryDataSource() {
        HikariDataSource mainDatasource = new HikariDataSource();
        mainDatasource.setDriverClassName(repoDriver);
        mainDatasource.setJdbcUrl(repoJdbcPath);
        mainDatasource.setUsername(repoUserName);
        mainDatasource.setPassword(repoPassword);
        mainDatasource.setIdleTimeout(repoIdleTimeoutMs);
        mainDatasource.setMaxLifetime(repoMaxLifeTimeMs);

        if (repoPoolSize > 0) {
            mainDatasource.setMaximumPoolSize(repoPoolSize);
        }
        //mainDatasource.setAutoCommit(false);
        return mainDatasource;
    }

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(jobRepositoryDataSource);
        factory.setTransactionManager(getTransactionManager());
        //to avoid deadlocks on the Job repo in SQL SERVER 2008
        factory.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ");
        factory.afterPropertiesSet();
        return factory.getObject();
    }
    
    @Override
    @Autowired
    public void setDataSource(@Qualifier("jobRepositoryDataSource")DataSource dataSource) {
        if (dataSource != null) {
            super.setDataSource(dataSource);
        }
    }


    @Bean
    public PlatformTransactionManager getJobRepoDataSourceTransactionManager(
            @Qualifier("jobRepositoryDataSource")
            DataSource jdbcDocumentTarget) {
        DataSourceTransactionManager tx = new DataSourceTransactionManager();
        tx.setDataSource(jdbcDocumentTarget);
        return tx;
    }

    
    @Bean
    public JobRegistry jobRegistry(){
        return new MapJobRegistry();
    }
    
    @Bean
    @Autowired
    public JobExplorer jobExplorer(@Qualifier("jobRepositoryDataSource")
                                               DataSource dataSource) throws Exception{
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public JobLauncher jobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        try {
            jobLauncher.afterPropertiesSet();
        } catch (Exception ex) {
            Logger.getLogger(BatchConfigurer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobLauncher;
    }
    
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }    
        
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
