package uk.ac.kcl.partitioners;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.kcl.listeners.JobCompleteNotificationListener;
import uk.ac.kcl.model.ScheduledPartitionParams;
import uk.ac.kcl.rowmappers.PartitionParamsRowMapper;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rich on 03/06/16.
 */
public abstract class AbstractRealTimeRangePartitioner implements RealtimeRangePartitioner {

    final static Logger logger = LoggerFactory.getLogger(RealtimeTimeStampAndPKRangePartitioner.class);

    @Autowired
    @Qualifier("sourceDataSource")
     DataSource sourceDataSource;

    @Autowired
     Environment env;

    @Autowired
     JobCompleteNotificationListener jobCompleteNotificationListener;

     String timeStamp;

    @PostConstruct
    public void init(){
        setColumn(env.getProperty("pkColumnNameToPartition"));
        setTable(env.getProperty("tableToPartition"));
        setTimeStampColumnName(env.getProperty("timeStampColumnNameToPersistInJobRepository"));
        if(env.getProperty("firstJobStartDate") !=null){
            firstRun = true;
        }else{
            firstRun = false;
        }
    }

    @Autowired
     BatchJobUtils batchJobUtils;

    JobExecution jobExecution;

    String table;

    String column;

    public void setTable(String table) {
        this.table = table;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setTimeStampColumnName(String timeStamp) {this.timeStamp = timeStamp;}

    boolean firstRun;


    abstract Map<String, ExecutionContext> getExecutionContextMap(int gridSize, Map<String, ExecutionContext> result);


    abstract Map<String, ExecutionContext> handleNoNewRecords(Map<String, ExecutionContext> map);

    abstract Map<String, ExecutionContext> getNextExecutionContextMapIfNoneFoundInPeriod(Map<String, ExecutionContext> result, Timestamp newestTimestampInDB);

    boolean noRecordsFoundInProcessingPeriod(ScheduledPartitionParams scheduledPartitionParams){
        if(scheduledPartitionParams.getMinTimeStamp() == null){
            return true;
        }else{
            return false;
        }
    }

    abstract ScheduledPartitionParams getParams(Timestamp timestamp);


    Timestamp getFirstTimestampInTable(JdbcTemplate jdbcTemplate) {
        Timestamp startTimeStamp;
        String tsSql = "SELECT MIN(" + timeStamp + ")  FROM " + table;
        startTimeStamp = jdbcTemplate.queryForObject(tsSql,Timestamp.class);
        return startTimeStamp;
    }

    Timestamp getFirstRunAsTimestamp() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(env.getProperty("datePatternForSQL"));
        DateTime dt = formatter.parseDateTime(env.getProperty("firstJobStartDate"));
        return new Timestamp(dt.getMillis());
    }

    Timestamp getEndTimeStamp(Timestamp startTimeStamp ){
        return new Timestamp(startTimeStamp.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
    }

    public void setJobExecution(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }
}
