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
import org.springframework.batch.core.launch.JobOperator;
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
import java.util.List;


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

    @Autowired
    JobOperator jobOperator;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SingleJobLauncher.class);


    public void launchJob()  {
        try {
                JobExecution lastJobExecution = null;
                ExitStatus lastJobExitStatus = null;
                try {
                    lastJobExecution = batchJobUtils.getLastCompletedFailedOrStoppedJobExecution();
                    lastJobExitStatus = lastJobExecution.getExitStatus();
                }catch(NullPointerException ex){
                    LOG.info("No previous completed jobs found");
                    jobOperator.startNextInstance(job.getName());
                }
                if(env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("false")){
                    LOG.info("Not using timeStampBasedScheduling");
                }else if(lastJobExitStatus == null){

                }else if (env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("true")){
                        switch (lastJobExitStatus.getExitCode()) {
                            case "COMPLETED":
                                jobOperator.startNextInstance(job.getName());
                                break;
                            case "EXECUTING":
                                LOG.info("Job is already running");
                                break;
                            case "FAILED":
                                LOG.info("Last job failed. Attempting restart");
                                jobOperator.restart(lastJobExecution.getId());
                                break;
                            case "NOOP":
                                break;
                            case "STOPPED":
                                LOG.info("Last job stopped. Attempting to restart incomplete steps");
                                jobOperator.restart(lastJobExecution.getId());
                                break;
                            case "UNKNOWN":
                                LOG.info("Last job has unknown status. Attempting restart from last job with known status");
                                throw new RuntimeException("unknown restarts not currently implemented");
//                                jobRepository.createJobExecution(job.getName(), )
//                                jobOperator.restart(lastJobExecution.getId());
//                                break;
                            default:
                                LOG.info("Should never be reached");
                                break;
                        }
                    }else {
                throw new RuntimeException("Cannot determine intended JobParameters");
            }
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
