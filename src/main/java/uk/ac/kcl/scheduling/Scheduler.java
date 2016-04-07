package uk.ac.kcl.scheduling;

import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import uk.ac.kcl.batch.ScheduledJobConfiguration;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
public class Scheduler {

    public Scheduler (boolean existing){
        this.existing = existing;
    }
    
    @Autowired
    Environment env;

    @Autowired
    JobOperator jobOperator;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;
    @Autowired
    JobExplorer jobExplorer;

    final static Logger logger = Logger.getLogger(ScheduledJobConfiguration.class);
    private boolean existing;

    @Scheduled(cron = "${scheduler.rate}")
    public void doTask()  {
        if (!existing) {
            JobParameters param = new JobParametersBuilder().addString("startTime", new Date().toString()).toJobParameters();
            try {
                JobExecution execution = jobLauncher.run(job, param);
                System.out.println(execution.getStatus().toString());
            } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException ex) {
                java.util.logging.Logger.getLogger(ScheduledJobConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
//            
//            Set<JobExecution> running =  jobExplorer.findRunningJobExecutions(env.getProperty("jobName"));
//            System.out.println("RUNNNNING JOBS");
//            System.out.println("Size" + running.size());
//            System.out.println(running.iterator().next().getJobParameters().toString());
//            try {
//                jobOperator.start(env.getProperty("jobName"), jobOperator.getParameters(running.iterator().next().getId()));
//                //jobLauncher.run(job, running.iterator().next().getJobParameters());
//            } catch (JobParametersInvalidException | NoSuchJobException | JobInstanceAlreadyExistsException | NoSuchJobExecutionException ex) {
//                java.util.logging.Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
//            }
        } 

    }
}
