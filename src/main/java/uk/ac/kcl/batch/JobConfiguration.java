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


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.jms.connection.CachingConnectionFactory;

import javax.sql.DataSource;
import java.util.ArrayList;

/**
 *
 * @author rich
 */

@EnableIntegration
@Configuration
@ComponentScan({"uk.ac.kcl.rowmappers","uk.ac.kcl.utils", "uk.ac.kcl.itemHandlers"})
@EnableBatchProcessing
@Import({
        DbLineFixerConfiguration.class,
        GateConfiguration.class,
        BasicJobConfiguration.class,
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
        props.setNullValue("");
        return props;
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



    @Value("${source.username}")
    String sourceUserName;
    @Value("${source.password}")
    String sourcePassword;

    @Bean(destroyMethod = "close")
    @Primary
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource() {
        BasicDataSource tempDatasource = new BasicDataSource();
        tempDatasource.setDriverClassName(env.getProperty("source.Driver"));
        tempDatasource.setUsername(sourceUserName);
        tempDatasource.setPassword(sourcePassword);
        tempDatasource.setUrl(env.getProperty("source.JdbcPath"));


        BasicDataSource mainDatasource = new BasicDataSource();
        executeSessionScripts(tempDatasource, mainDatasource);
        mainDatasource.setTestOnBorrow(true);
        mainDatasource.setValidationQuery("SELECT 1");
        mainDatasource.setDriverClassName(env.getProperty("source.Driver"));
        mainDatasource.setUrl(env.getProperty("source.JdbcPath"));
        mainDatasource.setUsername(sourceUserName);
        mainDatasource.setPassword(sourcePassword);



        return mainDatasource;
    }

    private void executeSessionScripts(BasicDataSource tempDatasource, BasicDataSource mainDatasource) {
        //temp datasource required to get type
        ArrayList<String> sqlStatements = new ArrayList<>();
        DatabaseType type = null;
        try {
            type = DatabaseType.fromMetaData(tempDatasource);

            switch (type) {
                case DERBY:
                    break;
                case DB2:
                    break;
                case DB2ZOS:
                    break;
                case HSQL:
                    break;
                case SQLSERVER:
                    sqlStatements.add("SET DATEFORMAT ymd;");
                    mainDatasource.setConnectionInitSqls(sqlStatements);
                    break;
                case MYSQL:
                    break;
                case ORACLE:
                    break;
                case POSTGRES:
                    break;
                case SYBASE:
                    break;
                case H2:
                    break;
                case SQLITE:
                    break;
            }
        } catch (MetaDataAccessException e) {
            e.printStackTrace();
        }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }


    @Value("${target.username}")
    String targetUserName;
    @Value("${target.password}")
    String targetPassword;

    @Bean(destroyMethod = "close")
    @Qualifier("targetDataSource")
    public DataSource targetDataSource() {

        BasicDataSource tempDatasource = new BasicDataSource();
        tempDatasource.setDriverClassName(env.getProperty("target.Driver"));
        tempDatasource.setUsername(targetUserName);
        tempDatasource.setPassword(targetPassword);
        tempDatasource.setUrl(env.getProperty("target.JdbcPath"));


        BasicDataSource mainDatasource = new BasicDataSource();
        executeSessionScripts(tempDatasource, mainDatasource);
        mainDatasource.setTestOnBorrow(true);
        mainDatasource.setValidationQuery("SELECT 1");
        mainDatasource.setDriverClassName(env.getProperty("target.Driver"));
        mainDatasource.setUrl(env.getProperty("target.JdbcPath"));
        mainDatasource.setUsername(targetUserName);
        mainDatasource.setPassword(targetPassword);

        return mainDatasource;
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
