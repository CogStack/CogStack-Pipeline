package uk.ac.kcl.utils;


import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;


/**
 * Created by rich on 21/04/16.
 */
@Service
public class BatchJobUtils {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BatchJobUtils.class);
    @Autowired
    Environment env;

    @Autowired
    @Qualifier("jobRepositoryDataSource")
    DataSource jobRepositoryDataSource;

    @Autowired
    @Qualifier("sourceDataSource")
    DataSource sourceDataSource;

    @Autowired
    private JobExplorer jobExplorer;

    public Timestamp checkForNewRecordsBeyondConfiguredProcessingPeriod(
            String tableName,
            Timestamp lastGoodJob,
            String timestampColumnName){
        JdbcTemplate template = new JdbcTemplate(sourceDataSource);
        String sql = "SELECT MIN(" + timestampColumnName + ") AS min_time_stamp " +
                " FROM " + tableName + " " +
                " WHERE CAST(" + timestampColumnName + " AS "+
                env.getProperty("source.dbmsToJavaSqlTimestampType")+
                ") >  CAST('" + lastGoodJob.toString() +
                "' AS "+env.getProperty("source.dbmsToJavaSqlTimestampType")+")";
        Timestamp timestampLong = template.queryForObject(sql, Timestamp.class);

        if(timestampLong == null){
            return null;
        }else {
            return new Timestamp(timestampLong.getTime());
        }
    }

//    public String getLastSuccessfulJobDate(){
//        JdbcTemplate template = new JdbcTemplate(targetDataSource);
//        String sql = "SELECT MAX(start_time) AS start_time FROM batch_job_execution bje \n" +
//                "JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id \n" +
//                "WHERE bje.exit_code = 'COMPLETED' AND bji.job_name = '" + env.getProperty("jobName") + "'";
//
//        String startTime = (String)template.queryForObject(sql, String.class);
//        return startTime;
//    }


    public JobExecution getLastJobExecution(){
        JdbcTemplate template = new JdbcTemplate(jobRepositoryDataSource);
        String sql = "SELECT MAX(bje.job_execution_id) FROM batch_job_execution bje \n" +
                " JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id \n" +
                " JOIN batch_job_execution_params bjep ON bje.job_execution_id = bjep.job_execution_id" +
                " WHERE bji.job_name = '" + env.getProperty("job.jobName") + "'";
        LOG.info("Looking for status of last job");
        Long id = template.queryForObject(sql, Long.class);
        return jobExplorer.getJobExecution(id);
    }

    public JobExecution getLastSuccessfulJobExecution(){
        JdbcTemplate template = new JdbcTemplate(jobRepositoryDataSource);
        String sql = "SELECT MAX(bje.job_execution_id) FROM batch_job_execution bje \n" +
                " JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id \n" +
                " JOIN batch_job_execution_params bjep ON bje.job_execution_id = bjep.job_execution_id" +
                " WHERE bje.status = 'COMPLETED' \n" +
                " AND bji.job_name = '" + env.getProperty("job.jobName") + "'";
        LOG.info("Looking for last successful job");
        Long id = template.queryForObject(sql, Long.class);
        return jobExplorer.getJobExecution(id);
    }

    public JobExecution getLastCompletedFailedOrStoppedJobExecution() {
        JdbcTemplate template = new JdbcTemplate(jobRepositoryDataSource);
        String sql = "SELECT MAX(bje.job_execution_id) FROM batch_job_execution bje \n" +
                " JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id \n" +
                " JOIN batch_job_execution_params bjep ON bje.job_execution_id = bjep.job_execution_id" +
                " WHERE (bje.exit_code = 'COMPLETED' OR bje.exit_code = 'FAILED' OR bje.exit_code = 'STOPPED') \n" +
                " AND bji.job_name = '" + env.getProperty("job.jobName") + "'";
        LOG.info("Looking for last restartable job");
        Long id =  template.queryForObject(sql, Long.class);
        return jobExplorer.getJobExecution(id);

    }

    public String cleanSqlString(String string){
        if (string == null ){
            return "";
        }else{
            return string;
        }
    }

    public List<Long> getExecutionIdsOfJobsToAbandon() {
        JdbcTemplate template = new JdbcTemplate(jobRepositoryDataSource);
        String sql = "SELECT bje.job_execution_id FROM batch_job_execution bje \n" +
                " JOIN batch_job_instance bji ON bje.job_instance_id = bji.job_instance_id \n" +
                " WHERE (" +
                "bje.status = 'FAILED' " +
                "OR bje.status = 'UNKNOWN' " +
                "OR bje.status = 'STARTING' " +
                "OR bje.status = 'STARTED'" +
                "OR bje.status = 'STOPPING') " +
                "AND bji.job_name = '" +
                env.getProperty("job.jobName") + "'";
        LOG.info("retrieving list of job executions to mark as abandoned " + sql);
        return template.queryForList(sql, Long.class);
    }
}
