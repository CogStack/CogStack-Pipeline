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

import org.postgresql.util.PSQLException;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.SQLException;


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



    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SingleJobLauncher.class);

    public void launchJob()  {
        try {
            //if (sourceDataSource.getConnection().isValid(10) && targetDataSource.getConnection().isValid(10)) {
                JobParameters param = new JobParametersBuilder()
                        .addDate("this_attempt_date", new Date(System.currentTimeMillis()))
                        .addString("jobClass", env.getProperty("jobClass"))
                        .toJobParameters();
                if (env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("true")) {
                    Object lastGoodJob = batchJobUtils.getLastSuccessfulRecordTimestamp();
                    LOG.info("Last good run was " + lastGoodJob + ". Recommencing from then");
                } else {
                    LOG.info("Not using timeStampBasedScheduling");
                }

                    JobExecution execution = jobLauncher.run(job, param);
                    System.out.println(execution.getStatus().toString());

            //}
        } catch (Exception ex){
            LOG.error("Cannot start job", ex);
        }
    }
}
