package uk.ac.kcl.utils;


import org.apache.log4j.Logger;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * Created by rich on 21/04/16.
 */
@Service
public class BatchJobUtils {
    final static Logger logger = Logger.getLogger(BatchJobUtils.class);
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

    public Timestamp convertStringToTimeStamp(String data) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(env.getProperty("datePatternForScheduling"));
        java.util.Date date = (java.util.Date)format.parse(data);
        Timestamp lastGoodDate = new Timestamp(date.getTime());
        return lastGoodDate;
    }

    public Timestamp getLastSuccessfulRecordTimestamp(){
        try {
            ExecutionContext ec = getLastSuccessfulJobExecutionContext();
            SimpleDateFormat format = new SimpleDateFormat(env.getProperty("datePatternForScheduling"));
            java.util.Date date = convertStringToTimeStamp(ec.get("last_successful_timestamp_from_this_job").toString());
            Timestamp lastGoodDate = new Timestamp(date.getTime());
            return lastGoodDate;
        }catch(NullPointerException ex){
            logger.info("No previous job finished. Starting from scratch");
        } catch (ParseException e) {
            logger.error("DATE PARSE EXCEPTION", e);
            System.exit(1);
        }
        return null;
    }

    public ExecutionContext getLastSuccessfulJobExecutionContext(){
        try {
            return jobExplorer.getJobExecution(getLastSuccessfulJobExecutionID()).getExecutionContext();
        }catch(NullPointerException ex){
            return null;
        }
    }

    public String cleanSqlString(String string){
        if (string == null ){
            return "";
        }else{
            return string;
        }
    }
}
