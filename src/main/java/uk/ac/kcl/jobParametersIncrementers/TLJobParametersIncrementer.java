package uk.ac.kcl.jobParametersIncrementers;

import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.ac.kcl.utils.BatchJobUtils;

import java.sql.Timestamp;

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

    private static final String RUN_ID_KEY = "run.id";

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
        BatchStatus lastJobStatus = null;
        JobExecution lastJobExecution = null;
        try {
            lastJobExecution = batchJobUtils.getLastJobExecution();
            lastJobStatus = lastJobExecution.getStatus();
        } catch (NullPointerException e) {
            LOG.info("No previous successful jobs found");
        }
        if(lastJobStatus == null) {
            params = getNewJobParameters(id,null);
        }else{
            switch (lastJobStatus) {
                case COMPLETED:
                    params = getNewJobParameters(id, lastJobExecution);
                    break;
                case STARTED:
                case STARTING:
                case STOPPING:
                case UNKNOWN:
                    LOG.error("Attempting to generate params but repository in unknown state");
                    break;
                case FAILED:
                case STOPPED:
                    params = lastJobExecution.getJobParameters();
                    break;
                case ABANDONED:
                    params = getNewJobParameters(id, batchJobUtils.getLastSuccessfulJobExecution());
                    break;
                default:
                    LOG.error("Should never be reached");
                    break;
            }
        }
        return params;
    }



    private JobParameters getNewJobParameters(long id, JobExecution lastJobExecution) {

        JobParameters params;
        Timestamp newJobTimeStamp;
        if(lastJobExecution == null){
            LOG.info("Cannot find any previously successful jobs. Commencing from beginning");
            params = new JobParametersBuilder()
                    .addString("first_run_of_job", "true")
                    .addString("jobName", env.getProperty("job.jobName"))
                    .addLong(key, id)
                    .toJobParameters();
        }else if(lastJobExecution.getExecutionContext().get("last_successful_timestamp_from_this_job")!=null){
            newJobTimeStamp = new Timestamp(
                    Long.valueOf(lastJobExecution
                            .getExecutionContext()
                            .get("last_successful_timestamp_from_this_job")
                            .toString()));
            LOG.info("Last good run was " + newJobTimeStamp.toString() + ". Recommencing from then");
            params = new JobParametersBuilder()
                    .addDate("last_timestamp_from_last_successful_job", newJobTimeStamp)
                    .addString("jobName",env.getProperty("job.jobName"))
                    .addLong(key, id)
                    .toJobParameters();
        }else{
            throw new RuntimeException("Cannot get new parameters from Job Repository");
        }
        return params;
    }
}
