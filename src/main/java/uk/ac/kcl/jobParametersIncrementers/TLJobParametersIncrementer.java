package uk.ac.kcl.jobParametersIncrementers;

import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by rich on 02/06/16.
 */
@Component
@Qualifier("tLJobParametersIncrementer")
public class TLJobParametersIncrementer implements JobParametersIncrementer {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TLJobParametersIncrementer.class);
    @Autowired
    Environment env;

    @Autowired
    JobExplorer jobExplorer;

    @Autowired
    JobOperator jobOperator;

    private static String RUN_ID_KEY = "run.id";

    private String key = RUN_ID_KEY;
    @Autowired
    private BatchJobUtils batchJobUtils;

    /**
     * The name of the run id in the job parameters.  Defaults to "run.id".
     *
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public JobParameters getNext(JobParameters parameters) {
        JobParameters params = (parameters == null) ? new JobParameters() : parameters;
        long id = params.getLong(key, 0L) + 1;
        ExitStatus lastJobExitStatus = null;
        JobExecution lastJobExecution = null;
        try {
            lastJobExecution = batchJobUtils.getLastJobExecution();
            lastJobExitStatus = lastJobExecution.getExitStatus();
        } catch (NullPointerException e) {
            LOG.info("No previous jobs found");
        }

        if(env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("false")){
            LOG.info("Not using timeStampBasedScheduling");
            params = new JobParametersBuilder(params)
                    .addLong(key, id)
                    .toJobParameters();
        }else if(lastJobExitStatus == null){
            params = new JobParametersBuilder()
                    .addString("first_run_of_job","true")
                    .addLong(key, id)
                    .toJobParameters();
        }else if (env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("true")){
            switch (lastJobExitStatus.getExitCode()) {
                case "COMPLETED":
                    LOG.info("Last job execution was successful");
                    params = getNewJobParameters(id, lastJobExecution);
                    break;
                case "EXECUTING":
                    LOG.info("Job is already running");
                    break;
                case "FAILED":
                    LOG.info("Last job failed. Attempting restart");
                    params = lastJobExecution.getJobParameters();
                    break;
                case "NOOP":
                    break;
                case "STOPPED":
                    LOG.info("Last job stopped. Attempting to restart incomplete steps");
                    params = lastJobExecution.getJobParameters();
                    break;
                case "UNKNOWN":
                    LOG.info("Last job has unknown status. Marking as abandoned and attempting restart from last successful job");
                    abandonAllJobsStartedAfterLastSuccessfulJob(lastJobExecution);
                    params = getNewJobParameters(id, batchJobUtils.getLastSuccessfulJobExecution());
                    break;
                default:
                    LOG.error("Should never be reached");
                    break;
            }
        }else {
            throw new RuntimeException("Cannot determine intended JobParameters");
        }
        return params;
    }

    private void abandonAllJobsStartedAfterLastSuccessfulJob(JobExecution lastJobExecution) {
        List<Long> idsToAbandon = batchJobUtils.getExecutionIdsOfFailedOrUnknownJobsAfterLastSuccessfulJob();

        for (Long id : idsToAbandon) {
            try {
                try {

                    jobOperator.stop(id);
                } catch (JobExecutionNotRunningException e) {
                    LOG.info("Cannot stop job execution ID " + id +
                            " as it is already stopped. Attempting to mark as abandoned");
                }
                try {
                    jobOperator.abandon(id);
                } catch (JobExecutionAlreadyRunningException e) {
                    throw new RuntimeException("Cannot abandon job execution ID " + id + " as it appears to be running. "+
                            "JobRepository may require inspection",e);
                }
            } catch (NoSuchJobExecutionException e) {
                throw new RuntimeException("Cannot mark job execution ID " + id + " as abandoned as it doesn't exist." +
                        " JobRepository may require inspection)", e);
            }
        }
    }

    private JobParameters getNewJobParameters(long id, JobExecution lastJobExecution) {
        JobParameters params;Timestamp lastSuccessfulItemTimestamp = new Timestamp(
                Long.valueOf(lastJobExecution
                        .getExecutionContext()
                        .get("last_successful_timestamp_from_this_job")
                        .toString()));
        LOG.info("Last good run was " + lastSuccessfulItemTimestamp.toString() + ". Recommencing from then");
        params = new JobParametersBuilder()
                .addDate("last_timestamp_from_last_successful_job", lastSuccessfulItemTimestamp)
                .addLong(key, id)
                .toJobParameters();
        return params;
    }

}
