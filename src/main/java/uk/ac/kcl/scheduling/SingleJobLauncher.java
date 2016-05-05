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

import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.utils.BatchJobUtils;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Service
@Import(JobConfiguration.class)
@ComponentScan({"uk.ac.kcl.utils"})
@EnableScheduling
public class SingleJobLauncher {


    @Autowired
    Environment env;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @Autowired
    BatchJobUtils batchJobUtils;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SingleJobLauncher.class);

    public void launchJob()  {
        JobParameters param = new JobParametersBuilder()
                .addDate("this_attempt_date",new Date())
                //.addDate("last_successful_record_date",batchJobUtils.getLastSuccessfulRecordTimestamp())
                .toJobParameters();
        if(env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("true")) {
            Object lastGoodJob = batchJobUtils.getLastSuccessfulRecordTimestamp();
            LOG.info("Last good run was " + lastGoodJob + ". Recommencing from then");
        }else{
            LOG.info("Not using timeStampBasedScheduling");
        }

        try {
            JobExecution execution = jobLauncher.run(job, param);
            System.out.println(execution.getStatus().toString());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException ex) {
            LOG.error("Cannot launch job",ex);
        }
    }
}
