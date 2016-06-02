package uk.ac.kcl.jobParametersIncrementers;

import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.sql.DataSource;
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
                    Timestamp lastSuccessfulItemTimestamp = new Timestamp(
                            Long.valueOf(lastJobExecution
                                    .getExecutionContext()
                                    .get("last_successful_timestamp_from_this_job")
                                    .toString()));

                    LOG.info("Last good run was " + lastSuccessfulItemTimestamp.toString() + ". Recommencing from then");
                    params = new JobParametersBuilder()
                            .addDate("last_timestamp_from_last_successful_job", lastSuccessfulItemTimestamp)
                            .addLong(key, id)
                            .toJobParameters();
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
                    LOG.info("Last job has unknown status. Attempting restart from beginning");
                    params = batchJobUtils.getLastCompletedFailedOrStoppedJobExecution().getJobParameters();
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
}
