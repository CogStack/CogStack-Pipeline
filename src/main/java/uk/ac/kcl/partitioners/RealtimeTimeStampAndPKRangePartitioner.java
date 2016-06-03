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
public class RealtimeTimeStampAndPKRangePartitioner extends AbstractRealTimeRangePartitioner{

    final static Logger logger = LoggerFactory.getLogger(RealtimeTimeStampAndPKRangePartitioner.class);
    private ScheduledPartitionParams params;
    private Timestamp jobStartTimeStamp;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
        logger.info("commencing timestamp and PK based partitioning");
        jobStartTimeStamp  = null;
        if(firstRun){
            jobStartTimeStamp = getFirstRunAsTimestamp();
        }else {
            try {
                jobStartTimeStamp = new Timestamp(jobExecution.getJobParameters()
                        .getDate("last_timestamp_from_last_successful_job").getTime());
            } catch (NullPointerException ex) {}
        }
        params = getParams(jobStartTimeStamp);
        if (noRecordsFoundInProcessingPeriod(params)) {
            result = handleNoNewRecords(result);
        } else {
            result = getExecutionContextMap(gridSize, result);
        }
        if(firstRun){
            firstRun=false;
        }
        return result;
    }


//////////////////////////////////    TEST THIS BLOCK

    @Override
    Map<String, ExecutionContext> handleNoNewRecords(Map<String, ExecutionContext> map) {
        Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
        Timestamp newestTimestampInDB;
        if(firstRun){
            newestTimestampInDB = getFirstRunAsTimestamp();
        }else{
            newestTimestampInDB = batchJobUtils.checkForNewRecordsBeyondConfiguredProcessingPeriod(
                    table, jobStartTimeStamp, timeStamp);
        }

        if(newestTimestampInDB==null){
            logger.info("Database appears to be synched as far as " +jobStartTimeStamp.toString()+ ". " +
                    "Checking again on next run");
            jobCompleteNotificationListener.setLastDateInthisJob(jobStartTimeStamp.getTime());
        }else {
            logger.info("New data found");
            result = getNextExecutionContextMapIfNoneFoundInPeriod(result, newestTimestampInDB);
        }
        return result;
    }

    @Override
    Map<String, ExecutionContext> getNextExecutionContextMapIfNoneFoundInPeriod(Map<String, ExecutionContext> result, Timestamp newestTimestampInDB) {
        ScheduledPartitionParams params = getParams(newestTimestampInDB);
        result = getExecutionContextMap(Integer.valueOf(env.getProperty("gridSize")),result);

        logger.info("No data found this job. Next earliest timestamp is " + String.valueOf(newestTimestampInDB.toString()));
        jobCompleteNotificationListener.setLastDateInthisJob(params.getMaxTimeStamp().getTime()-1L);
        return result;
    }


////////////////////////////

    @Override
    Map<String, ExecutionContext> getExecutionContextMap(int gridSize, Map<String, ExecutionContext> result) {
        logger.info("Database not yet synched. Synching as far as  "
                + params.getMaxTimeStamp().toString() + " this job");
        jobCompleteNotificationListener.setLastDateInthisJob(params.getMaxTimeStamp().getTime());

        long targetSize = (params.getMaxId() - params.getMinId()) / gridSize + 1;
        long start = params.getMinId();
        long end = start + targetSize - 1;
        logger.info("Commencing timestamp ordered PK partitioning");
        if ((params.getMaxId() -params.getMinId()) < (long) gridSize) {
            long partitionCount = (params.getMaxId() -params.getMaxId());
            logger.info("There are fewer new records than the grid size. Expect only " + partitionCount+ "partitions this execution") ;
            for(long i = 0;i<(partitionCount);i++) {
                ExecutionContext value = new ExecutionContext();
                result.put("partition" + (i + 1L), value);
                value.putLong("minValue", (params.getMinId()+1L+i) );
                value.putLong("maxValue", (params.getMinId()+1L+i) );
                value.put("min_time_stamp", params.getMinTimeStamp().toString());
                value.put("max_time_stamp", params.getMaxTimeStamp().toString());
            }
        }else{
            logger.info("Generating " +gridSize+" partitions");
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





    @Override
    ScheduledPartitionParams getParams(Timestamp timestamp) {
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
        if(env.getProperty("firstJobStartDate") !=null && firstRun){
            logger.info ("firstJobStartDate detected in configs. Commencing from " + env.getProperty("firstJobStartDate"));
            Timestamp earliestRecord = getFirstRunAsTimestamp();
            jobEndTimeStamp = new Timestamp(earliestRecord.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    "\nBETWEEN CAST ('" + earliestRecord.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST ('" + jobEndTimeStamp.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    "\n ORDER BY " + timeStamp +" ASC " +" , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
        } else if(timestamp != null) {
            logger.info ("last successful batch retrieved from job repository. Commencing from after " + timestamp.toString());
            jobEndTimeStamp = getEndTimeStamp(timestamp);
            sql =	sql +
                    "\n WHERE CAST (" + timeStamp + " as "+
                    env.getProperty("dbmsToJavaSqlTimestampType")+
                    " ) > CAST ('" + timestamp.toString()  +
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
            timestamp = getFirstTimestampInTable(jdbcTemplate);
            jobEndTimeStamp = getEndTimeStamp(timestamp);
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) BETWEEN CAST ('" +
                    timestamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST ('" + jobEndTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " ORDER BY " + timeStamp  +" ASC " + " , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
            logger.info ("No previous successful batches detected. Commencing from first timestamp: " + timestamp.toString());
        }else{
            logger.error("unable to determine partition requirement");
        }
        logger.info ("This job SQL: " + sql);
        return (ScheduledPartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());
    }
}