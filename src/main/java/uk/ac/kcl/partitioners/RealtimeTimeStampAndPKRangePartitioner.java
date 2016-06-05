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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.ScheduledPartitionParams;
import uk.ac.kcl.rowmappers.PartitionParamsRowMapper;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


@Service("columnRangePartitioner")
@ComponentScan("uk.ac.kcl.listeners")
@org.springframework.context.annotation.Profile("primaryKeyAndTimeStampPartition")
public class RealtimeTimeStampAndPKRangePartitioner extends AbstractRealTimeRangePartitioner implements Partitioner{

    final static Logger logger = LoggerFactory.getLogger(RealtimeTimeStampAndPKRangePartitioner.class);

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result;
        ScheduledPartitionParams params;
        Timestamp startTimestamp = getStartTimeStampIfConfiguredOrFirstRun();
        if(startTimestamp != null){
            params = getParams(startTimestamp,true);
        }else{
            startTimestamp = getLastTimestampFromLastSuccessfulJob();
            params = getParams(startTimestamp,false);
        }
        if(noRecordsFoundInProcessingPeriod(params)){
            params = scanForNewRecords(startTimestamp);
        }

        result = getExecutionContextMap(gridSize,params);
        if(firstRun){
            firstRun =false;
        }
        return result;
    }






    private ScheduledPartitionParams getParams(Timestamp jobStartTimeStamp, boolean inclusiveOfStart) {
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
        Timestamp jobEndTimeStamp = jobEndTimeStamp =getEndTimeStamp(jobStartTimeStamp);
        if(configuredFirstRunTimestamp!= null && firstRun){
            sql = getStartTimeInclusiveSqlString(sql, jobStartTimeStamp, jobEndTimeStamp);
        } else if(inclusiveOfStart) {
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


//////////////////////////////////    TEST THIS BLOCK

    ScheduledPartitionParams scanForNewRecords(Timestamp jobStartTimeStamp) {
        ScheduledPartitionParams result = null;
        logger.info("No new data found in processing period " + String.valueOf(jobStartTimeStamp.toString())
                +" to " +getEndTimeStamp(jobStartTimeStamp).toString()+". Commencing scan ahead");
        jobStartTimeStamp = batchJobUtils.checkForNewRecordsBeyondConfiguredProcessingPeriod(
                table, jobStartTimeStamp, timeStamp);

        if(jobStartTimeStamp == null){
            logger.info("Database appears to be synched as far as " +jobStartTimeStamp.toString()+ ". " +
                    "Checking again on next run");
            jobCompleteNotificationListener.setLastDateInthisJob(jobStartTimeStamp.getTime());
        }else{
            logger.info("New data found! Next job will start from " + jobStartTimeStamp.toString());
            result =  getParams(jobStartTimeStamp,true);
        }
        return result;
    }


////////////////////////////

    Map<String, ExecutionContext> getExecutionContextMap(int gridSize, ScheduledPartitionParams params) {
        logger.info("commencing timestamp and PK based partitioning");
        Map<String,ExecutionContext> result = new HashMap<>();
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




    private String getStartTimeExclusiveSqlString(String sql, Timestamp jobStartTimeStamp, Timestamp jobEndTimeStamp) {
        sql =	sql +
                " WHERE CAST (" + timeStamp + " as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) " +
                "> CAST ('" +
                jobStartTimeStamp.toString() +
                "' as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) " +
                " AND CAST (" + timeStamp + " as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) " +
                " <= CAST ('" + jobEndTimeStamp.toString() +
                "' as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) ) t1";
        return sql;
    }
    private String getStartTimeInclusiveSqlString(String sql, Timestamp jobStartTimeStamp, Timestamp jobEndTimeStamp) {
        sql =	sql +
                " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                "\nBETWEEN CAST ('" + jobStartTimeStamp.toString() +
                "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                " AND CAST ('" + jobEndTimeStamp.toString() +
                "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                " ) t1" ;
        return sql;
    }

}