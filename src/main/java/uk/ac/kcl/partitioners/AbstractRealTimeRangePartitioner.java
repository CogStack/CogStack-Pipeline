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
import org.springframework.beans.factory.annotation.Value;
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
import java.text.MessageFormat;
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

    @Value("${timeStampColumnNameToPersistInJobRepository}")
    String timeStamp;

    @Value("${dbmsToJavaSqlTimestampType}")
    String dbmsToJavaSqlTimestampType ;

    @Value("${firstJobStartDate:#{null}}")
    String firstJobStartDate;

    @Value("${checkForEmptyPartitions:#{false}}")
    Boolean checkForEmptyPartitions;

    @Value("${datePatternForSQL:#{null}}")
    String datePatternForSQL;

    @Value("${tableToPartition}")
    String table;

    @Value("${pkColumnNameToPartition}")
    String column;

    @Value("${processingPeriod}")
    Long processingPeriod;

    @Value("${maxPartitionSize:#{null}}")
    Long maxPartitionSize;

    JdbcTemplate jdbcTemplate;
    Timestamp configuredFirstRunTimestamp;
    boolean firstRun;

    @PostConstruct
    public void init(){
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

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    boolean noRecordsFoundInProcessingPeriod(ScheduledPartitionParams scheduledPartitionParams){
        return scheduledPartitionParams.getMinTimeStamp() == null;
    }

    private Timestamp getFirstTimestampInTable() {
        Timestamp startTimeStamp = null;
        try{
            if (jobExecution.getJobParameters().getString("first_run_of_job").equalsIgnoreCase("true")) {
                String tsSql = MessageFormat.format("SELECT MIN({0})  FROM {1}",timeStamp,table);
                startTimeStamp = jdbcTemplate.queryForObject(tsSql, Timestamp.class);
                firstRun = true;
            }
        }catch (NullPointerException ignored){}

        return startTimeStamp;
    }

    private Timestamp getConfiguredRunAsTimestamp() {
        Timestamp timestamp = null;
        try {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(datePatternForSQL);
            DateTime dt = formatter.parseDateTime(firstJobStartDate);
            timestamp = new Timestamp(dt.getMillis());
        }catch(NullPointerException ignored){}
        return timestamp;
    }



    Timestamp getEndTimeStamp(Timestamp startTimeStamp){
        return new Timestamp(startTimeStamp.getTime() + processingPeriod);
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
            logger.info ("No previous successful batches detected. Commencing from first timestamp: "
                    + startTimestamp.toString());
            return startTimestamp;
        }
        return null;
    }
    Map<String, ExecutionContext> getMap(int gridSize, ScheduledPartitionParams params) {
        Map<String, ExecutionContext> result = new HashMap<>();
        if ((params.getMaxId() -params.getMinId()) < (long) gridSize) {
            long partitionCount = (params.getMaxId() -params.getMinId()+1L);
            logger.info("There are fewer new records than the grid size. Expect only " + partitionCount+
                    " partitions this execution") ;
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
            if(maxPartitionSize!=null){
                logger.info("maxPartitionSize detected in properties. Ignoring gridSize if configured");
                long targetSize = maxPartitionSize;
                long start = params.getMinId();
                long end = params.getMinId() + targetSize;
                int partitionCounter = 0;
                while (start <= params.getMaxId()) {
                    if(populateMap(params, result, start, end, partitionCounter)){
                        partitionCounter++;
                    }
                    start += targetSize;
                    end += targetSize;
                }
            }else {
                long targetSize = (params.getMaxId() - params.getMinId()) / gridSize + 1;
                long start = params.getMinId();
                long end = start + targetSize - 1;
                int counter = 0;
                for (int i = 0; i < gridSize; i++) {
                    if(populateMap(params, result, start, end, counter)){
                        counter++;
                    }
                    start += targetSize;
                    end += targetSize;
                }
            }
        }
        if (params.getMaxTimeStamp() !=null){
            jobCompleteNotificationListener.setLastDateInthisJob(params.getMaxTimeStamp().getTime());
        }
        logger.info("partitioning complete");
        return result;
    }

    private boolean populateMap(ScheduledPartitionParams params, Map<String, ExecutionContext> result,
                                long start, long end, int counter) {

        if(checkForEmptyPartitions) {
            long recordCountThisPartition = getRecordCountThisPartition(Long.toString(start), Long.toString(end),
                    params.getMinTimeStamp().toString(),
                    params.getMaxTimeStamp().toString());
            if (recordCountThisPartition > 0L) {
                result.put("partition" + counter, getNewExecutionContext(params, start, end));
                logger.info("partition " + counter + " created");
                return true;
            } else {
                return false;
            }
        }else{
            result.put("partition" + counter, getNewExecutionContext(params, start, end));
            logger.info("partition " + counter + " created");
            return true;
        }
    }


    private ExecutionContext getNewExecutionContext(ScheduledPartitionParams params, long start, long end) {
        ExecutionContext value = new ExecutionContext();
        value.putLong("minValue", start);
        value.putLong("maxValue", end);
        value.put("min_time_stamp", params.getMinTimeStamp().toString());
        value.put("max_time_stamp", params.getMaxTimeStamp().toString());
        return value;
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
            tsSql = tsSql + MessageFormat.format(" WHERE {0} " +
                    " BETWEEN CAST(''{1}'' AS {2}) " +
                    " AND CAST(''{3}'' AS {2}) " +
                    " AND {4} BETWEEN ''{5}'' AND ''{6}''", timeStamp,minTimeStamp, dbmsToJavaSqlTimestampType,
                    maxTimeStamp,column,minValue,maxValue);
        }
        long partitionCount = jdbcTemplate.queryForObject(tsSql, Long.class);
        if(partitionCount==0L){
            logger.debug("No rows detected with query " + tsSql);
        }else{
            logger.info( partitionCount +" rows detected with query " + tsSql);
        }
        return partitionCount;
    }
}
