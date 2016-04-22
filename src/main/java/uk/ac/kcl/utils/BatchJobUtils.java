package uk.ac.kcl.utils;


import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Date;



/**
 * Created by rich on 21/04/16.
 */
@Service
public class BatchJobUtils {

    @Autowired
    Environment env;

    @Autowired
    @Qualifier("targetDataSource")
    DataSource targetDataSource;

    @Autowired
    private JobExplorer jobExplorer;

    public String getLastSuccessfulJobDate(){
        JdbcTemplate template = new JdbcTemplate(targetDataSource);
        String sql = "select max(start_time) AS start_time from batch_job_execution bje \n" +
                "join batch_job_instance bji on bje.job_instance_id = bji.job_instance_id \n" +
                "where bje.exit_code = 'COMPLETED' and bji.job_name = '" + env.getProperty("jobName") + "'";

        String startTime = (String)template.queryForObject(sql, String.class);
        return startTime;
    }

    public Long getLastSuccessfulJobExecutionID(){
        JdbcTemplate template = new JdbcTemplate(targetDataSource);
        String sql = "select max(job_execution_id) AS start_time from batch_job_execution bje \n" +
                "join batch_job_instance bji on bje.job_instance_id = bji.job_instance_id \n" +
                "where bje.exit_code = 'COMPLETED' and bji.job_name = '" + env.getProperty("jobName") + "'";

        Long id = (Long)template.queryForObject(sql, Long.class);
        return id;
    }

    public Object getLastSuccessfulRecordDate(){
        ExecutionContext ec = getLastSuccessfulJobExecutionContext();
        Object lastGoodDate =  ec.get("last_successful_record_date_from_this_job");
        return lastGoodDate;

    }

    public ExecutionContext getLastSuccessfulJobExecutionContext(){

        return jobExplorer.getJobExecution(getLastSuccessfulJobExecutionID()).getExecutionContext();
    }
}
