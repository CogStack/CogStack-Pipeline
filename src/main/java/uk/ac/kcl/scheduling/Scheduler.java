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
package uk.ac.kcl.scheduling;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import uk.ac.kcl.batch.ScheduledJobConfiguration;
import utils.BatchJobUtils;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */

public class Scheduler {

    
    @Autowired
    Environment env;


    @Autowired
    JobExplorer jobExplorer;
    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    final static Logger logger = Logger.getLogger(Scheduler.class);

    @Scheduled(cron = "${scheduler.rate}")
    public void doTask()  {


        JobParameters param = new JobParametersBuilder().addString("previousSuccessfulJobStartTime", BatchJobUtils.getLastSuccessfulJobDate()).toJobParameters();
            try {
                JobExecution execution = jobLauncher.run(job, param);
                System.out.println(execution.getStatus().toString());
            } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException ex) {
                java.util.logging.Logger.getLogger(ScheduledJobConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}
