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
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import uk.ac.kcl.jobParametersIncrementers.TLJobParametersIncrementer;
import uk.ac.kcl.listeners.JobCompleteNotificationListener;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Profile("localPartitioning")
@ComponentScan({"uk.ac.kcl.partitioners",
        "uk.ac.kcl.listeners",
        "uk.ac.kcl.jobParametersIncrementers"})
@Configuration
public class LocalConfiguration {

    @Autowired
    Environment env;

    @Value("${partitioner.gridSize:1}")
    int gridSize;

    @Value("${job.jobName:defaultJob}")
    String jobName;


    @Bean
    public TaskExecutorPartitionHandler partitionHandler(
            @Qualifier("compositeSlaveStep")Step compositeSlaveStep,
            @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setGridSize(gridSize);
        handler.setStep(compositeSlaveStep);
        handler.setTaskExecutor(taskExecutor);
        return handler;
    }

    @Bean
    public Job job(JobBuilderFactory jobs,
                   StepBuilderFactory steps,
                   Partitioner partitioner,
                   JobCompleteNotificationListener jobCompleteNotificationListener,
                   @Qualifier("partitionHandler") PartitionHandler partitionHandler,
                   @Qualifier("tLJobParametersIncrementer") TLJobParametersIncrementer runIdIncrementer

    ) {
        return jobs.get(env.getProperty("job.jobName"))
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
