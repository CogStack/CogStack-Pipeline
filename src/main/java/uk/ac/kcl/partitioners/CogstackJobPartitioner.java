package uk.ac.kcl.partitioners;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.partition.support.Partitioner;
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
import uk.ac.kcl.rowmappers.PartitionParamsRowMapper;
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
@Service("cogstackPartitioner")
@ComponentScan("uk.ac.kcl.listeners")
public  class CogstackJobPartitioner implements Partitioner {

    private final static Logger logger = LoggerFactory.getLogger(CogstackJobPartitioner.class);

    @Autowired
    JobCompleteNotificationListener jobCompleteNotificationListener;

    @Autowired
    protected BatchJobUtils batchJobUtils;

    @Autowired
    @Qualifier("sourceDataSource")
    protected DataSource sourceDataSource;

    @Autowired
    protected Environment env;

    @Value("${partitioner.timeStampColumnName:#{null}}")
    String timeStamp;

    @Value("${source.dbmsToJavaSqlTimestampType}")
    String dbmsToJavaSqlTimestampType ;

    @Value("${configuredStart.firstJobStartDate:#{null}}")
    String firstJobStartDate;

    @Value("${checkForEmptyPartitions:#{false}}")
    Boolean checkForEmptyPartitions;

    @Value("${configuredStart.datePatternForSQL:#{null}}")
    String datePatternForSQL;

    @Value("${partitioner.tableToPartition}")
    String table;

    @Value("${partitioner.pkColumnName}")
    String column;

    @Value("${scheduler.processingPeriod:777600000000}")
    Long processingPeriod;

    @Value("${partitioner.maxPartitionSize:#{null}}")
    Long maxPartitionSize;

    @Value("${partitioner.partitionType}")
    String partitionType;

    @Value("${partitioner.preFieldsSQL:#{null}}")
    String preFieldsSQL;


    JobExecution jobExecution;
    JdbcTemplate jdbcTemplate;
    boolean firstRun;

    @PostConstruct
    public void init(){
        this.jdbcTemplate = new JdbcTemplate(sourceDataSource);
        firstRun = true;
    }


    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = null;
        //determine whether to use PKs and Timestamps, or just PKs for partitioning logic
        switch (partitionType) {
            case "PK":
                throw new UnsupportedOperationException("PK only partitions currently not supported");
            case "PKTimeStamp":
                result = configureForPKTimeStampPartitions(gridSize);
                break;
            default:
                throw new RuntimeException("Partition type not specified");
        }
        return result;
    }

    private Map<String, ExecutionContext> configureForPKTimeStampPartitions(int gridSize) {
        Map<String, ExecutionContext> result;
        ScheduledPartitionParams params;
        Timestamp startTimestamp;
        if(firstJobStartDate!=null && firstRun) {
            //if there's a configured start timestamp, use this
            startTimestamp = getConfiguredRunAsTimestamp();
            params = getParams(startTimestamp, true);
            logger.info ("configuredStart.firstJobStartDate detected in config. Commencing from " + startTimestamp.toString());
        }else if(jobExecution.getJobParameters().getString("last_timestamp_from_last_successful_job") ==null){
//            or this is the first ever JobExecution, get the timestamp for this
            startTimestamp = getFirstTimestampInTable();
            params = getParams(startTimestamp, true);
            logger.info("No previous successful batches detected. Commencing from first timestamp: "
                    + startTimestamp.toString());
        }else {
            //otherwise get timestamp from last good job
            startTimestamp = getLastTimestampFromLastSuccessfulJob();
            params = getParams(startTimestamp, false);
        }

        if (noRecordsFoundInProcessingPeriod(params)) {
            //with the selected job parameters, if there's no new data found in the configured processing period,
            //scan ahead for new data
            params = scanForNewRecords(startTimestamp);
        }
        if (params != null) {
            //make execution context with parameters if new data found
            logger.info("commencing timestamp and PK based partitioning");
            result = getMap(gridSize,params);
            //also tell teh job about the oldest timestamp found this JobExecution
            informJobCompleteListenerOfLastDate(params.getMaxTimeStamp());
        } else {
            //if no data found, DB is synched, and just return an empty map
            result = new HashMap<>();
        }
        return result;
    }



    private ScheduledPartitionParams getParams(Timestamp jobStartTimeStamp, boolean inclusiveOfStart) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        String sql = MessageFormat.format("SELECT MAX({0}) AS max_id, MIN({0}) AS min_id, MAX({1}) AS max_time_stamp " +
                        ",MIN({1}) AS min_time_stamp FROM ( SELECT {2} {0},{1} FROM {3} ",
                column,timeStamp,batchJobUtils.cleanSqlString(preFieldsSQL),table);

        Timestamp jobEndTimeStamp = getEndTimeStamp(jobStartTimeStamp);
        if(inclusiveOfStart) {
            sql = getStartTimeInclusiveSqlString(sql, jobStartTimeStamp, jobEndTimeStamp);
        }else if(!inclusiveOfStart){
            sql = getStartTimeExclusiveSqlString(sql, jobStartTimeStamp, jobEndTimeStamp);
        }else{
            throw new RuntimeException("cannot determine parameters");
        }
        logger.info ("This job SQL: " + sql);
        return (ScheduledPartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());
    }



    private ScheduledPartitionParams scanForNewRecords(Timestamp jobStartTimeStamp) {
        ScheduledPartitionParams result = null;
        logger.info("No new data found in processing period " + String.valueOf(jobStartTimeStamp.toString())
                +" to " +getEndTimeStamp(jobStartTimeStamp).toString()+". Commencing scan ahead");
        Timestamp newJobStartTimeStamp = batchJobUtils.checkForNewRecordsBeyondConfiguredProcessingPeriod(
                table, jobStartTimeStamp, timeStamp);

        if(newJobStartTimeStamp == null){
            logger.info("Database appears to be synched as far as " +jobStartTimeStamp.toString()+ ". " +
                    "Checking again on next run");
            informJobCompleteListenerOfLastDate(jobStartTimeStamp);
        }else{
            logger.info("New data found! Generating partitions from " + newJobStartTimeStamp.toString() +" inclusive.");
            result =  getParams(newJobStartTimeStamp,true);
        }
        return result;
    }





    private String getStartTimeExclusiveSqlString(String sql, Timestamp jobStartTimeStamp, Timestamp jobEndTimeStamp) {
        sql =	sql + MessageFormat.format(" WHERE CAST ({0} as {1}) > CAST (''{2}'' AS {1} ) " +
                        " AND CAST ({0} AS {1}) <= CAST (''{3}'' AS {1}) )t1 ",
                timeStamp,dbmsToJavaSqlTimestampType,jobStartTimeStamp.toString(),jobEndTimeStamp.toString());
        return sql;
    }
    private String getStartTimeInclusiveSqlString(String sql, Timestamp jobStartTimeStamp, Timestamp jobEndTimeStamp) {
        sql =sql + MessageFormat.format(" WHERE CAST ({0} as {1}) BETWEEN CAST (''{2}'' AS {1} ) " +
                        " AND CAST (''{3}'' AS {1})  )t1 ",
                timeStamp,dbmsToJavaSqlTimestampType,jobStartTimeStamp.toString(),jobEndTimeStamp.toString());
        return sql;
    }

    private Map<String, ExecutionContext> getMap(int gridSize, ScheduledPartitionParams params) {
        Map<String, ExecutionContext> result = new HashMap<>();
        if ((params.getMaxId() -params.getMinId() +1) <=  (gridSize+1)) {
            populateContextMapWithPartitionCountLimit(params, result);
        } else {
            populateContextMapWithAllPartitions(gridSize, params, result);
        }
        if (params.getMaxTimeStamp() !=null){
            informJobCompleteListenerOfLastDate(params.getMaxTimeStamp());
        }
        logger.info("partitioning complete");
        return result;
    }

    private void populateContextMapWithAllPartitions(int gridSize, ScheduledPartitionParams params, Map<String, ExecutionContext> result) {
        if(maxPartitionSize!=null){
            populateMapWithMaximumConfiguredPartitionLimit(params, result);
        }else {
            populateMapWithNoPartitionCountLimit(gridSize, params, result);
        }
    }

    private void populateMapWithNoPartitionCountLimit(int gridSize, ScheduledPartitionParams params, Map<String, ExecutionContext> result) {
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

    private void populateMapWithMaximumConfiguredPartitionLimit(ScheduledPartitionParams params, Map<String, ExecutionContext> result) {
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
    }

    private void populateContextMapWithPartitionCountLimit(ScheduledPartitionParams params, Map<String, ExecutionContext> result) {
        long partitionCount = (params.getMaxId() -params.getMinId()+1);
        logger.info("There are fewer or equal new records than the grid size. Expect " + partitionCount+
                " partitions this execution") ;
        for(long i = 0;i<(partitionCount);i++) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + (i + 1L), value);
            value.putLong("minValue", (params.getMinId()+i) );
            value.putLong("maxValue", (params.getMinId()+i) );
            value.put("min_time_stamp", params.getMinTimeStamp().toString());
            value.put("max_time_stamp", params.getMaxTimeStamp().toString());
        }
    }

    private boolean noRecordsFoundInProcessingPeriod(ScheduledPartitionParams scheduledPartitionParams){
        return scheduledPartitionParams.getMinTimeStamp() == null;
    }

    private Timestamp getFirstTimestampInTable() {
        String tsSql = MessageFormat.format("SELECT MIN({0})  FROM {1}",timeStamp,table);
        Timestamp startTimeStamp  = jdbcTemplate.queryForObject(tsSql, Timestamp.class);
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



    private Timestamp getEndTimeStamp(Timestamp startTimeStamp){
        return new Timestamp(startTimeStamp.getTime() + processingPeriod);
    }

    private Timestamp getLastTimestampFromLastSuccessfulJob(){
        Timestamp jobStartTimeStamp  = new Timestamp(jobExecution.getJobParameters()
                .getDate("last_timestamp_from_last_successful_job").getTime());
        return jobStartTimeStamp;
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


    private long getRecordCountThisPartition(String minValue, String maxValue, String minTimeStamp, String maxTimeStamp){
        String tsSql = "SELECT COUNT(*)  FROM " + table;
        if( minTimeStamp!= null && maxTimeStamp != null) {
            tsSql = tsSql + MessageFormat.format(" WHERE {0} " +
                            " BETWEEN CAST(''{1}'' AS {2}) " +
                            " AND CAST(''{3}'' AS {2}) " +
                            " AND {4} BETWEEN ''{5}'' AND ''{6}''",
                    timeStamp,minTimeStamp, dbmsToJavaSqlTimestampType,
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

    public void setJobExecution(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }
}
