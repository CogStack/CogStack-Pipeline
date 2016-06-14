package uk.ac.kcl.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;

import javax.annotation.PreDestroy;
import java.util.Set;

/**
 * Created by rich on 14/06/16.
 */
@Service
public class CleanupBean {
    private static final Logger LOG = LoggerFactory.getLogger(CleanupBean.class);
    @Autowired
    JobOperator jobOperator;
    @Autowired
    JobExplorer jobExplorer;

    @Autowired(required = false)
    ScheduledJobLauncher scheduledJobLauncher;


    @Autowired
    private Environment env;

    @PreDestroy
    private void cleanup(){
        LOG.info("stopping scheduler");
        if(scheduledJobLauncher!=null){
            scheduledJobLauncher.setContinueWork(false);
        }

        LOG.info("Attempting to stop running jobs");
        Set<Long> jobExecs = null;
        try {
            jobExecs = jobOperator.getRunningExecutions(env.getProperty("jobName"));
        } catch (NoSuchJobException e) {
            LOG.error("Couldn't get job list to stop executions ",e);
        }

        if(jobExecs.size()==0) {
            LOG.info("No running jobs detected. Exiting now");
            return;
        };

        for(Long l : jobExecs){

            try{
                jobOperator.stop(l);
            } catch (JobExecutionNotRunningException|NoSuchJobExecutionException e) {
                LOG.error("Couldn't stop job ",e);
            }
        }

        int stoppedCount = 0;
        stop_loop:
        for(int i=0;i<=5;i++){
            for(Long l : jobExecs){
                JobExecution exec = jobExplorer.getJobExecution(l);
                BatchStatus status = exec.getStatus();
                LOG.info("Job name "+ exec.getJobInstance().getJobName() +" has status of "+ status );
                if (status == BatchStatus.STOPPED ||
                        status == BatchStatus.FAILED ||
                        status == BatchStatus.COMPLETED ||
                        status == BatchStatus.ABANDONED ) {
                    stoppedCount++;
                }
                if(stoppedCount == jobExecs.size()) break stop_loop;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.warn("program exited before all jobs confirmed stopped. Job Repository may be in unknown state");
                break;
            }
        }
        if(stoppedCount == jobExecs.size()){
            LOG.info("Jobs successfully stopped, completed or are known to have failed");
        }else {
            LOG.warn("Unable to gracefully stop jobs. Job Repository may be in unknown state");
        }
    }
}
