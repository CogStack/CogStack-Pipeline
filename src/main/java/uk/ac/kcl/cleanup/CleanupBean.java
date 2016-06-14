package uk.ac.kcl.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

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
    private Environment env;

    @PreDestroy
    private void cleanup(){
        LOG.info("Attempting to stop running jobs");
        Set<Long> jobExecs = null;
        try {
            jobExecs = jobOperator.getRunningExecutions(env.getProperty("jobName"));
        } catch (NoSuchJobException e) {
            LOG.error("Couldn't get job list to stop executions ",e);
        }

        if(jobExecs.size()==0) LOG.info("No running jobs detected. Exiting now");

        for(Long l : jobExecs){

            try{
                jobOperator.stop(l);
            } catch (JobExecutionNotRunningException|NoSuchJobExecutionException e) {
                LOG.error("Couldn't stop job ",e);
            }
        }
    }
}
