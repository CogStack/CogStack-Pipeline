package uk.ac.kcl.utils;


import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.service.GateService;

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
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BatchJobUtils.class);
    @Autowired
    Environment env;

    @Autowired
    @Qualifier("targetDataSource")
    DataSource targetDataSource;

    @Autowired
    private JobExplorer jobExplorer;

    public Timestamp checkForNewRecordsBeyondConfiguredProcessingPeriod(
            String tableName,
            Timestamp lastestTimestamp,
            String timestampColumnName){
        JdbcTemplate template = new JdbcTemplate(targetDataSource);
        String sql = "select MIN(" + timestampColumnName + ") AS min_time_stamp " +
                    " FROM " + tableName + " " +
                    " WHERE CAST(" + timestampColumnName + " AS "+env.getProperty("dbmsToJavaSqlTimestampType")+") >  CAST('" + lastestTimestamp.toString() + "' AS "+env.getProperty("dbmsToJavaSqlTimestampType")+")";
        Timestamp timestampLong = (Timestamp)template.queryForObject(sql, Timestamp.class);

        if(timestampLong == null){
            return null;
        }else {
            return new Timestamp(timestampLong.getTime());
        }
    }

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


    public String convertTimeStampToESDateFormat(Timestamp ts) {
        SimpleDateFormat format = new SimpleDateFormat(env.getProperty("datePatternForES"));
        //java.util.Date date = new java.util.Date(ts.getTime());
        String parseResult = format.format(ts);
        return parseResult;
    }

    public Timestamp getLastSuccessfulRecordTimestamp(){

            ExecutionContext ec = getLastSuccessfulJobExecutionContext();
//            SimpleDateFormat format = new SimpleDateFormat(env.getProperty("datePatternForScheduling"));
//            java.util.Date date = convertStringToTimeStamp();
            if(ec == null){
                LOG.info("No previous job found in job repository");
                return null;
            }else{
                Timestamp lastGoodDate = new Timestamp(Long.parseLong(ec.get("last_successful_timestamp_from_this_job").toString()));
                return lastGoodDate;
            }
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
