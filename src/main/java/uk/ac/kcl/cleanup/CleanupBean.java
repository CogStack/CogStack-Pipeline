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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.exception.CogstackException;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rich on 14/06/16.
 */
@Service
public class CleanupBean implements SmartLifecycle, ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(CleanupBean.class);
    @Autowired
    JobOperator jobOperator;
    @Autowired
    JobExplorer jobExplorer;

    @Autowired(required = false)
    ScheduledJobLauncher scheduledJobLauncher;

    private ApplicationContext applicationContext;

    @Value("${job.jobName:defaultJob}")
    String jobName;

    public void setJobExecutionId(long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    long jobExecutionId;

    boolean running;

    @Autowired
    private Environment env;


    private void cleanup(){
        LOG.info("stopping scheduler");
        if(scheduledJobLauncher!=null){
            scheduledJobLauncher.setContinueWork(false);
        }
        LOG.info("Attempting to stop running jobs");
        Set<Long> jobExecs = new HashSet<>();
        try {

            jobExecs.addAll(jobOperator.getRunningExecutions(jobName));
        } catch (NoSuchJobException e) {
            LOG.error("Couldn't get job list to stop executions ",e);
        } catch (NullPointerException ex){
            //probably no running jobs?
        }



        if(jobExecs.size() == 0) {
            LOG.info("No running jobs detected. Exiting now");
            return;
        }else if(jobExecs.size() > 1){
            LOG.warn("Detected more than one "+jobName+ " with status of running.");
        };


        boolean stopped;
        for(Long l : jobExecs){
            try{
                stopped = jobOperator.stop(l);
                if(stopped){
                    LOG.info("Stop message successfully sent to repository");
                }
            } catch (NoSuchJobExecutionException e) {
                LOG.error("Job no longer exists ",e);
            }catch(JobExecutionNotRunningException e){
                LOG.info("Job is no longer running ",e);
            }
        }



        RetryTemplate retryTemplate = getRetryTemplate();
        LOG.info("Waiting for job to stop");
        boolean confirmedStopped = false;

        try {
            confirmedStopped =retryTemplate.execute(new RetryCallback<Boolean,CogstackException>() {
                public Boolean doWithRetry(RetryContext context) {
                    // business logic here
                    for(Long l : jobExecs){
                        JobExecution exec = jobExplorer.getJobExecution(l);
                        BatchStatus status = exec.getStatus();
                        LOG.info("Job name "+ exec.getJobInstance().getJobName() +" has status of "+ status );
                        if (status == BatchStatus.STOPPED ||
                                status == BatchStatus.FAILED ||
                                status == BatchStatus.COMPLETED ||
                                status == BatchStatus.ABANDONED ) {
                            return true;
                        }
                    }
                    throw new CogstackException("Job did not stop");
                }
            }, new RecoveryCallback() {
                @Override
                public Object recover(RetryContext context) throws CogstackException {
                    //maybe add logic to abandon job?
                    LOG.info("Unable to gracefully stop jobs. Job Repository may be in unknown state",context.getLastThrowable());
                    return context;
                }
            });
        } catch (CogstackException e) {
            LOG.warn("Unable to gracefully stop jobs. Job Repository may be in unknown state");
        }

        if(confirmedStopped){
            LOG.info("Job successfully stopped, completed or are known to have failed");
        }
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        LOG.info("****************CONTROLLED SHUTDOWN INITIATED*********************\n\n" +
                "Hit quit command to terminate immediatly");
        cleanup();
        stop();
        callback.run();
    }

    @Override
    public void start() {
        LOG.info("****************STARTUP INITIATED*********************");
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    public RetryTemplate getRetryTemplate(){
//        TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
//        retryPolicy.setTimeout(Long.valueOf(env.getProperty("shutdownTimeout")));
        AlwaysRetryPolicy retryPolicy = new AlwaysRetryPolicy();
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(5000);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
