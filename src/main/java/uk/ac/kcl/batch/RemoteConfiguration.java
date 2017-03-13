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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import uk.ac.kcl.jobParametersIncrementers.TLJobParametersIncrementer;
import uk.ac.kcl.listeners.JobCompleteNotificationListener;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Profile("remotePartitioning")
@ImportResource({"classpath:spring-master.xml","classpath:spring-slave.xml"})
@ComponentScan({"uk.ac.kcl.partitioners",
        "uk.ac.kcl.listeners",
        "uk.ac.kcl.jobParametersIncrementers"})
@EnableIntegration
@Configuration
public class RemoteConfiguration {

    @Autowired
    Environment env;
    @Value("${partitioner.gridSize:3}")
    private int gridSize;

    @Value("${job.jobName:defaultJob}")
    private String jobName;


    @Value("${jms.ip}")
    private String jmsip;
    @Value("${jms.username")
    private String jmsUsername;
    @Value("${jms.password")
    private String jmsPassword;
    @Value("${jms.closeTimeout")
    private int jmsCloseTimeout;
    @Value("${partitioner.partitionHandlerTimeout}")
    private int partitionHandlerTimeout;



    @Bean
    @Qualifier("partitionHandler")
    public MessageChannelPartitionHandler partitionHandler(
            @Qualifier("requestChannel") MessageChannel reqChannel,
            @Qualifier("aggregatedReplyChannel") PollableChannel repChannel) {
        MessageChannelPartitionHandler handler = new MessageChannelPartitionHandler();
        handler.setGridSize(gridSize);
        handler.setStepName("compositeSlaveStep");
        handler.setReplyChannel(repChannel);
        MessagingTemplate template = new MessagingTemplate();
        template.setDefaultChannel(reqChannel);
        template.setReceiveTimeout(partitionHandlerTimeout);
        handler.setMessagingOperations(template);
        return handler;
    }
    @Bean
    public CachingConnectionFactory connectionFactory(ActiveMQConnectionFactory factory){
        return new CachingConnectionFactory(factory);
    }

    @Bean
    public ActiveMQConnectionFactory amqConnectionFactory(){
        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(jmsip);
        factory.setUserName(jmsUsername);
        factory.setPassword(jmsPassword);
        factory.setCloseTimeout(jmsCloseTimeout);
        return factory;
    }
    @Bean
    public Job job(JobBuilderFactory jobs,
                   StepBuilderFactory steps,
                   Partitioner partitioner,
                   @Qualifier("partitionHandler") PartitionHandler partitionHandler,
                   JobCompleteNotificationListener jobCompleteNotificationListener,
                   @Qualifier("tLJobParametersIncrementer") TLJobParametersIncrementer runIdIncrementer

                   ) {
        return jobs.get(jobName)
                .incrementer(runIdIncrementer)
                .listener(jobCompleteNotificationListener)
                .flow(
                        steps
                                .get(jobName + "MasterStep")
                                .partitioner((jobName+"SlaveStep"), partitioner)
                                .partitionHandler(partitionHandler)
                                .build()
                )
                .end()
                .build();

    }
}
