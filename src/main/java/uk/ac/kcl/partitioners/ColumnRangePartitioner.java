/* 
 * Copyright 2016 King's College London, Richard Jackson <richgjackson@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.kcl.partitioners;

import org.apache.commons.net.ntp.TimeStamp;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.listeners.JobCompleteNotificationListener;
import uk.ac.kcl.model.PartitionParams;
import uk.ac.kcl.model.ScheduledPartitionParams;
import uk.ac.kcl.rowmappers.PartitionParamsRowMapper;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


@Service("columnRangePartitioner")
@ComponentScan("uk.ac.kcl.listeners")
public class ColumnRangePartitioner implements Partitioner {

    final static Logger logger = LoggerFactory.getLogger(ColumnRangePartitioner.class);

    @Autowired
    @Qualifier("sourceDataSource")
    private DataSource sourceDataSource;

    @Autowired
    private     Environment env;

    @Autowired
    private JobCompleteNotificationListener jobCompleteNotificationListener;

    private String timeStamp;

    @PostConstruct
    public void init(){
        setColumn(env.getProperty("pkColumnNameToPartition"));
        setTable(env.getProperty("tableToPartition"));
        setTimeStampColumnName(env.getProperty("timeStampColumnNameToPartition"));
    }

    @Autowired
    private  BatchJobUtils batchJobUtils;

    private JobExecution jobExecution;

    private String table;

    private String column;

    public void setTable(String table) {
        this.table = table;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setTimeStampColumnName(String timeStamp) {this.timeStamp = timeStamp;}

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
        if (env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("false")) {
            logger.info("Commencing PK only partition");
            PartitionParams params = getUnscheduledPartitionParams();
            if(params.getMinId() == params.getMaxId()){
                logger.info("Only one step to generate this job");
                ExecutionContext value = new ExecutionContext();
                result.put("partition " + 1, value);
                value.putLong("minValue", params.getMinId());
                value.putLong("maxValue", params.getMaxId());
                value.putString("note", "this job generated only one slave step");
            }else {
                logger.info("Multiple steps to generate this job");
                long targetSize = (params.getMaxId() - params.getMinId()) / gridSize + 1;
                long start = params.getMinId();
                long end = start + targetSize - 1;
                for (int i = 0; i < gridSize; i++) {
                    ExecutionContext value = new ExecutionContext();
                    result.put("partition" + (i + 1), value);
                    value.putLong("minValue", start);
                    value.putLong("maxValue", end);
                    start += targetSize;
                    end += targetSize;
                }
            }
            logger.info("partitioning complete");
            return result;
        }else{
            logger.info("commencing timestamp based partitioning");
            ///TEST TOMORROW

            Timestamp jobStartTimeStamp  = null;
            try {
                jobStartTimeStamp = new Timestamp(jobExecution.getJobParameters()
                        .getDate("last_timestamp_from_last_successful_job").getTime());
            }catch(NullPointerException ex){};
            //Timestamp jobStartTimeStamp = batchJobUtils.getOldestTimeStampInLastSuccessfulJob();
            ///

            ScheduledPartitionParams params = getScheduledPartitionParams(jobStartTimeStamp);
            if (noRecordsFoundInProcessingPeriod(params)) {

                Timestamp newestTimestampInDB = batchJobUtils.checkForNewRecordsBeyondConfiguredProcessingPeriod(
                        table, jobStartTimeStamp, timeStamp);
                if (newestTimestampInDB == null) {
                    jobCompleteNotificationListener.setLastDateInthisJob(jobStartTimeStamp.getTime());
                    logger.info("Database appears to be synched as far as " + String.valueOf(jobStartTimeStamp.toString())
                            + "Checking again on next job");
                    return result;
                } else {
                    logger.info("New data found! Next job will synch as far as " + String.valueOf(newestTimestampInDB.toString()));
                    jobCompleteNotificationListener.setLastDateInthisJob(newestTimestampInDB.getTime());
                }
                return result;
            } else {
                logger.info("Database not yet synched. Synching as far as  "
                        + params.getMaxTimeStamp().toString() + " this job");
                jobCompleteNotificationListener.setLastDateInthisJob(params.getMaxTimeStamp().getTime());

                long targetSize = (params.getMaxId() - params.getMinId()) / gridSize + 1;
                long start = params.getMinId();
                long end = start + targetSize - 1;
                logger.info("Commencing timestamp ordered PK partitioning");
                if(params.getMinId()==params.getMaxId()) {
                    logger.info("Only one step to generate this job");
                    ExecutionContext value = new ExecutionContext();
                    result.put("partition " + 1, value);
                    value.putLong("minValue", params.getMinId());
                    value.putLong("maxValue", params.getMaxId());
                    value.put("min_time_stamp", params.getMinTimeStamp().toString());
                    value.put("max_time_stamp", params.getMaxTimeStamp().toString());
                    value.putString("note", "this job generated only one slave step");
                }else{
                    logger.info("Multiple steps to generate this job");
                    for (int i = 0; i < gridSize; i++) {
                        ExecutionContext value = new ExecutionContext();
                        result.put("partition" + (i + 1), value);
                        value.putLong("minValue", start);
                        value.putLong("maxValue", end);
                        value.put("min_time_stamp", params.getMinTimeStamp().toString());
                        value.put("max_time_stamp", params.getMaxTimeStamp().toString());
                        start += targetSize;
                        end += targetSize;
                    }
                }
                logger.info("partitioning complete");
                return result;
            }
        }
    }

    private PartitionParams getUnscheduledPartitionParams() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        String sql = " SELECT "   +
                " MAX(" + column + ") AS max_id , " +
                " MIN(" + column + ") AS min_id  " +
                " FROM " + table + " ";

        return (PartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());


    }

    private boolean noRecordsFoundInProcessingPeriod(ScheduledPartitionParams scheduledPartitionParams){
        if(scheduledPartitionParams.getMinTimeStamp() == null){
            return true;
        }else{
            return false;
        }
    }

    private ScheduledPartitionParams getScheduledPartitionParams(Timestamp startTimeStamp) {
        Timestamp jobEndTimeStamp;
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        String sql = "\n SELECT "   +
                " MAX(" + column + ") AS max_id , \n" +
                " MIN(" + column + ") AS min_id , \n" +
                " MAX(" + timeStamp + ") AS max_time_stamp , \n" +
                " MIN(" + timeStamp + ") AS min_time_stamp  \n" +
                " FROM ( \n" +
                " SELECT " +
                batchJobUtils.cleanSqlString(env.getProperty("partitionerPreFieldsSQL")) +
                " " + column + ", " + timeStamp +
                " FROM " + table + " ";
        if(env.getProperty("firstJobStartDate") !=null){
            logger.info ("firstJobStartDate detected in configs. Commencing from " + env.getProperty("firstJobStartDate"));
            DateTimeFormatter formatter = DateTimeFormat.forPattern(env.getProperty("datePatternForSQL"));
            DateTime dt = formatter.parseDateTime(env.getProperty("firstJobStartDate"));
            Timestamp earliestRecord = new Timestamp(dt.getMillis());
            jobEndTimeStamp = new Timestamp(earliestRecord.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    "\nBETWEEN CAST ('" + env.getProperty("firstJobStartDate") +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST ('" + jobEndTimeStamp.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    "\n ORDER BY " + timeStamp +" ASC " +" , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
        } else if(startTimeStamp != null) {
            logger.info ("last successful batch retrieved from job repository. Commencing from after " + startTimeStamp.toString());
            jobEndTimeStamp = getEndTimeStamp(startTimeStamp);
            sql =	sql +
                    "\n WHERE CAST (" + timeStamp + " as "+
                    env.getProperty("dbmsToJavaSqlTimestampType")+
                    " ) > CAST ('" + startTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    "\n AND CAST ("  + timeStamp +  " as "+
                    env.getProperty("dbmsToJavaSqlTimestampType")+
                    " ) <= CAST ('" + jobEndTimeStamp.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+
                    " ) " +
                    "\n ORDER BY " + timeStamp  +" ASC " + " , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
        }else if(jobExecution.getJobParameters().getString("first_run_of_job").equalsIgnoreCase("true")){
            String tsSql = "SELECT MIN(" + timeStamp + ")  FROM " + table;
            startTimeStamp = jdbcTemplate.queryForObject(tsSql,Timestamp.class);
            jobEndTimeStamp = getEndTimeStamp(startTimeStamp);
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) BETWEEN CAST ('" +
                    startTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST ('" + jobEndTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " ORDER BY " + timeStamp  +" ASC " + " , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
            logger.info ("No previous successful batches detected. Commencing from first timestamp: " + startTimeStamp.toString());
        }else{
            logger.error("unable to determine partition requirement");
        }
        logger.info ("This job SQL: " + sql);
        return (ScheduledPartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());
    }

    private Timestamp getEndTimeStamp(Timestamp startTimeStamp ){
        return new Timestamp(startTimeStamp.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
    }

    public void setJobExecution(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }
}