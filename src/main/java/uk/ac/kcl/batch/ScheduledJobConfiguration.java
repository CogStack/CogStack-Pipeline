package uk.ac.kcl.batch;

import java.util.Date;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Configuration
@Import(JobConfiguration.class)
@EnableScheduling
public class ScheduledJobConfiguration {

    @Autowired
    Environment env;

    
    final static Logger logger = Logger.getLogger(ScheduledJobConfiguration.class);

    @Scheduled(fixedRate = 10)
    public void doTask() {
        
        System.out.println("Working");

    }
}
