package uk.ac.kcl.partitioners;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.listeners.JobCompleteNotificationListener;
import uk.ac.kcl.model.ScheduledPartitionParams;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Timestamp;

/**
 * Created by rich on 03/06/16.
 */
@Service("abstractRealTimeRangePartitioner")
@ComponentScan("uk.ac.kcl.listeners")
public abstract class AbstractRealTimeRangePartitioner {

    private final static Logger logger = LoggerFactory.getLogger(AbstractRealTimeRangePartitioner.class);

    @Autowired
    JobCompleteNotificationListener jobCompleteNotificationListener;
    @Autowired
    @Qualifier("sourceDataSource")
    protected DataSource sourceDataSource;

    @Autowired
    protected Environment env;

    String timeStamp;

    private JdbcTemplate jdbcTemplate;
    Timestamp configuredFirstRunTimestamp;

    @PostConstruct
    public void init(){
        setColumn(env.getProperty("pkColumnNameToPartition"));
        setTable(env.getProperty("tableToPartition"));
        setTimeStampColumnName(env.getProperty("timeStampColumnNameToPersistInJobRepository"));
        if(env.getProperty("firstJobStartDate") !=null){
            configuredFirstRunTimestamp = getConfiguredRunAsTimestamp();
            firstRun = true;
        }else{
            firstRun = false;
        }
        this.jdbcTemplate = new JdbcTemplate(sourceDataSource);
    }

    @Autowired
    protected BatchJobUtils batchJobUtils;

    private JobExecution jobExecution;

    String table;

    String column;

    private void setTable(String table) {
        this.table = table;
    }

    private void setColumn(String column) {
        this.column = column;
    }

    private void setTimeStampColumnName(String timeStamp) {this.timeStamp = timeStamp;}

    boolean firstRun;


    boolean noRecordsFoundInProcessingPeriod(ScheduledPartitionParams scheduledPartitionParams){
        return scheduledPartitionParams.getMinTimeStamp() == null;
    }

    private Timestamp getFirstTimestampInTable() {
        Timestamp startTimeStamp = null;
        try{
            if (jobExecution.getJobParameters().getString("first_run_of_job").equalsIgnoreCase("true")) {
                String tsSql = "SELECT MIN(" + timeStamp + ")  FROM " + table;
                startTimeStamp = jdbcTemplate.queryForObject(tsSql, Timestamp.class);
                firstRun = true;
            }
        }catch (NullPointerException ignored){}

        return startTimeStamp;
    }

    private Timestamp getConfiguredRunAsTimestamp() {
        Timestamp timestamp = null;
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(env.getProperty("datePatternForSQL"));
            DateTime dt = formatter.parseDateTime(env.getProperty("firstJobStartDate"));
            timestamp = new Timestamp(dt.getMillis());
        }catch(NullPointerException ignored){}
        return timestamp;
    }



    Timestamp getEndTimeStamp(Timestamp startTimeStamp){
        return new Timestamp(startTimeStamp.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
    }

    Timestamp getLastTimestampFromLastSuccessfulJob(){
        Timestamp jobStartTimeStamp  = null;
        try {
            jobStartTimeStamp = new Timestamp(jobExecution.getJobParameters()
                    .getDate("last_timestamp_from_last_successful_job").getTime());
        }catch(NullPointerException ignored){}
        return jobStartTimeStamp;
    }

    Timestamp getStartTimeStampIfConfiguredOrFirstRun(){
        Timestamp startTimestamp;
        if(configuredFirstRunTimestamp !=null && firstRun){
            startTimestamp =  configuredFirstRunTimestamp;
            logger.info ("firstJobStartDate detected in configs. Commencing from " + startTimestamp.toString());
            return startTimestamp;
        }
        startTimestamp = getFirstTimestampInTable();
        if(startTimestamp != null){
            logger.info ("No previous successful batches detected. Commencing from first timestamp: " + startTimestamp.toString());
            return startTimestamp;
        }
        return null;
    }

    public void setJobExecution(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }
}
