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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import uk.ac.kcl.scheduling.Scheduler;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Profile("master")
@ImportResource("classpath:spring-master.xml")
@Configuration
public class MasterIntegrationConfiguration {

    @Bean
    public Scheduler scheduler() {
        return new Scheduler();
    }

    @Autowired
    Environment env;

    @Bean
    public MessageChannelPartitionHandler partitionHandler(
            @Qualifier("requestChannel") MessageChannel reqChannel,
            @Qualifier("aggregatedReplyChannel") PollableChannel repChannel) {
        MessageChannelPartitionHandler handler = new MessageChannelPartitionHandler();
        handler.setGridSize(Integer.parseInt(env.getProperty("gridSize")));
        handler.setStepName(env.getProperty("stepName"));
        handler.setReplyChannel(repChannel);
        MessagingTemplate template = new MessagingTemplate();
        template.setDefaultChannel(reqChannel);
        template.setReceiveTimeout(Integer.parseInt(env.getProperty("partitionHandlerTimeout")));
        handler.setMessagingOperations(template);

        return handler;
    }

    @Configuration
    @Profile("gate")
    public static class GateJobMaster {

        @Bean
        public Job gateJob(JobBuilderFactory jobs,
                StepBuilderFactory steps,
                Partitioner partitioner,
                @Qualifier("partitionHandler") PartitionHandler gatePartitionHandler) {
            Job job = jobs.get("gateJob")
                    .incrementer(new RunIdIncrementer())
                    .flow(
                            steps
                            .get("gateMasterStep")
                            .partitioner("gateSlaveStep", partitioner)
                            .partitionHandler(gatePartitionHandler)
                            .build()
                    )
                    .end()
                    .build();
            return job;

        }
    }

    @Configuration
    @Profile("tika")
    public static class TikaJobMaster {

        @Bean
        public Job tikaJob(JobBuilderFactory jobs,
                StepBuilderFactory steps,
                Partitioner partitioner,
                @Qualifier("partitionHandler") PartitionHandler partitionHandler) {
            Job job = jobs.get("tikaJob")
                    .incrementer(new RunIdIncrementer())
                    .flow(
                            steps
                            .get("tikaMasterStep")
                            .partitioner("tikaSlaveStep", partitioner)
                            .partitionHandler(partitionHandler)
                            .build()
                    )
                    .end()
                    .build();
            return job;

        }

    }

    @Configuration
    @Profile("dBLineFixer")
    public static class DBLineFixerMaster {

        @Bean
        public Job dBLineFixerJob(JobBuilderFactory jobs,
                StepBuilderFactory steps,
                Partitioner partitioner,
                @Qualifier("partitionHandler") PartitionHandler gatePartitionHandler) {
            Job job = jobs.get("dBLineFixerJob")
                    .incrementer(new RunIdIncrementer())
                    .flow(
                            steps
                            .get("dBLineFixerMasterStep")
                            .partitioner("dBLineFixerSlaveStep", partitioner)
                            .partitionHandler(gatePartitionHandler)
                            .build()
                    )
                    .end()
                    .build();
            return job;

        }

    }

}
