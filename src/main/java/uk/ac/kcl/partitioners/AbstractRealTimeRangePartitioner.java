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
import java.util.HashMap;
import java.util.Map;

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

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

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
    Map<String, ExecutionContext> getMap(int gridSize, ScheduledPartitionParams params) {
        Map<String, ExecutionContext> result = new HashMap<>();
        if ((params.getMaxId() -params.getMinId()) < (long) gridSize) {
            long partitionCount = (params.getMaxId() -params.getMinId()+1L);
            logger.info("There are fewer new records than the grid size. Expect only " + partitionCount+ " partitions this execution") ;
            for(long i = 0;i<(partitionCount);i++) {
                ExecutionContext value = new ExecutionContext();
                result.put("partition" + (i + 1L), value);
                value.putLong("minValue", (params.getMinId()+i) );
                value.putLong("maxValue", (params.getMinId()+i) );
                value.put("min_time_stamp", params.getMinTimeStamp().toString());
                value.put("max_time_stamp", params.getMaxTimeStamp().toString());
            }
        } else {
            logger.info("Multiple steps to generate this job");
            if(env.getProperty("maxPartitionSize")!=null){
                populatePartitionMapWithRestrictions(params, result);
            }else {
                populatePartitionMapWithoutRestrictions(gridSize, params, result);
            }
        }
        if (params.getMaxTimeStamp() !=null){
            jobCompleteNotificationListener.setLastDateInthisJob(params.getMaxTimeStamp().getTime());
        }
        logger.info("partitioning complete");
        return result;
    }

    private void populatePartitionMapWithRestrictions(ScheduledPartitionParams params, Map<String, ExecutionContext> result) {
        long targetSize = Long.valueOf(env.getProperty("maxPartitionSize"));
        long start = params.getMinId();
        long end = targetSize;
        int partitionCounter = 0;
        while (start <= params.getMaxId()) {
            ExecutionContext value = new ExecutionContext();
            long recordCountThisPartition = getRecordCountThisPartition(
                    String.valueOf(start),
                    String.valueOf(end),
                    params.getMinTimeStamp().toString(),
                    params.getMaxTimeStamp().toString());
            addParams(params, result, start, end, partitionCounter, value, recordCountThisPartition);
            start += targetSize;
            end += targetSize;
            partitionCounter++;
        }
    }

    private void addParams(ScheduledPartitionParams params, Map<String, ExecutionContext> result, long start, long end, int partitionCounter, ExecutionContext value, long recordCountThisPartition) {
        if (recordCountThisPartition == 0L) {
            //move to next partition
        } else {
            result.put("partition" + (partitionCounter + 1), value);
            value.putLong("minValue", start);
            value.putLong("maxValue", end);
            value.put("min_time_stamp", params.getMinTimeStamp().toString());
            value.put("max_time_stamp", params.getMaxTimeStamp().toString());
        }
    }

    private void populatePartitionMapWithoutRestrictions(int gridSize, ScheduledPartitionParams params, Map<String, ExecutionContext> result) {
        long targetSize = (params.getMaxId() - params.getMinId()) / gridSize + 1;
        long start = params.getMinId();
        long end = start + targetSize - 1;
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext value = new ExecutionContext();
            long recordCountThisPartition = getRecordCountThisPartition(Long.toString(start), Long.toString(end),
                    params.getMinTimeStamp().toString(),
                    params.getMaxTimeStamp().toString());
            addParams(params, result, start, end, i, value, recordCountThisPartition);
            start += targetSize;
            end += targetSize;
        }
    }

    public void informJobCompleteListenerOfLastDate(Timestamp jobEndTimestamp) {
        jobCompleteNotificationListener.setLastDateInthisJob(jobEndTimestamp.getTime());
    }
    public void setJobExecution(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    private long getRecordCountThisPartition(String minValue, String maxValue, String minTimeStamp, String maxTimeStamp){
        String tsSql = "SELECT COUNT(*)  FROM " + table;

        if( minTimeStamp!= null && maxTimeStamp != null) {
            tsSql = tsSql + " WHERE " +env.getProperty("timeStamp")
                    + " BETWEEN CAST('" + minTimeStamp +
                    "' AS "+env.getProperty("dbmsToJavaSqlTimestampType")+") "
                    + " AND CAST('" + maxTimeStamp +
                    "' AS "+env.getProperty("dbmsToJavaSqlTimestampType")+") "
                    + " AND " + env.getProperty("columnToProcess")
                    + " BETWEEN '" + minValue + "' AND '" + maxValue +"'";
        }
        long partitionCount = jdbcTemplate.queryForObject(tsSql, Long.class);
        if(partitionCount==0L){
            logger.info("No rows detected with query " + tsSql);
        }else{
            logger.info( partitionCount +" rows detected with query " + tsSql);
        }
        return partitionCount;
    }
}
