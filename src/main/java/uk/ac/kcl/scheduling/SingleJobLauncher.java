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

import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Timestamp;


/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Service
@Import(JobConfiguration.class)
@ComponentScan({"uk.ac.kcl.utils"})
public class SingleJobLauncher {


    @Autowired
    Environment env;

    @Autowired(required=false)
    JobLauncher jobLauncher;

    @Autowired(required=false)
    Job job;

    @Autowired(required=false)
    BatchJobUtils batchJobUtils;

    @Autowired
    @Qualifier("targetDataSource")
    DataSource targetDataSource;

    @Autowired
    @Qualifier("sourceDataSource")
    DataSource sourceDataSource;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobExplorer jobExplorer;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SingleJobLauncher.class);


    public void launchJob()  {
        JobParameters param;
        try {
            param = new JobParametersBuilder()
                    .addDate("this_attempt_date", new Date(System.currentTimeMillis()))
                    .addString("jobClass", env.getProperty("jobClass"))
                    .toJobParameters();


            if (env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("true")) {

                Timestamp lastGoodJob = batchJobUtils.getOldestTimeStampInLastSuccessfulJob();
                ///Get last job EXECUTION ID. NEEDS TESTING! instance !=execution ID.
                JobExecution lastJobExecution = jobExplorer.getJobExecution(((long) jobExplorer.getJobInstanceCount(env.getProperty("jobClass"))));
                switch(lastJobExecution.getExitStatus().toString()){
                    case "COMPLETED":
                        LOG.info("Last good run was " + lastJobExecution.getJobParameters().getDate("last_successful_timestamp_from_this_job") + ". Recommencing from then");
                        param = new JobParametersBuilder()
                                .addDate("last_timestamp_from_last_successful_job", lastJobExecution.getJobParameters()
                                        .getDate("last_successful_timestamp_from_this_job"))
                                .addString("jobClass", env.getProperty("jobClass"))
                                .toJobParameters();
                        break;
                    case "EXECUTING":
                        LOG.info("Job is already running");
                        break;
                    case "FAILED":
                        LOG.info("Last job failed. Attempting restart");
                        param = lastJobExecution.getJobParameters();
                        break;
                    case "NOOP":
                        break;
                    case "STOPPED":
                        LOG.info("Last job stopped. Attempting to restart incomplete steps");
                        break;
                    case "UNKNOWN":
                        LOG.info("Last job has unknown status. Attempting restart from beginning");
                        break;
                    default:
                        LOG.info("No previous jobs found. Launching first job");
                        break;
                }
            } else {
                LOG.info("Not using timeStampBasedScheduling");
            }
            JobExecution execution = jobLauncher.run(job, param);
            LOG.info(execution.getStatus().toString());
        } catch (JobInstanceAlreadyCompleteException|
                JobExecutionAlreadyRunningException|
                JobParametersInvalidException|
                JobRestartException e) {
            LOG.error("Cannot start job", e);
        } catch (Exception e) {
            LOG.error("Cannot start job", e);
        }
    }
}
