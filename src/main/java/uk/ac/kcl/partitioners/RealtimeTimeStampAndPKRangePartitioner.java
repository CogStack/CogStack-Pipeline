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
        Map<String, ExecutionContext> result = null;
        logger.info("commencing timestamp and PK based partitioning");

        ScheduledPartitionParams params = getParams();
        if (noRecordsFoundInProcessingPeriod(params)) {
            result = handleNoNewRecords(params);
        } else {
            result = getExecutionContextMap(gridSize, params);
        }
        if(configuredRunFirstRun){
            configuredRunFirstRun =false;
        }
        return result;
    }


//////////////////////////////////    TEST THIS BLOCK

    Map<String, ExecutionContext> handleNoNewRecords(ScheduledPartitionParams params) {
        Map<String, ExecutionContext> result = new HashMap<>();
        Timestamp jobStartTimeStamp = null;
        if(configuredRunFirstRun){
            jobStartTimeStamp = getConfiguredRunAsTimestamp();
            logger.info("No data found in specified processing period of configured start date");
        }else{
            jobStartTimeStamp = batchJobUtils.checkForNewRecordsBeyondConfiguredProcessingPeriod(
                    table, params.getMinTimeStamp(), timeStamp);
        }
        if(jobStartTimeStamp==null){
            logger.info("Database appears to be synched as far as " +params.getMaxTimeStamp().getTime()+ ". " +
                    "Checking again on next run");
            jobCompleteNotificationListener.setLastDateInthisJob(params.getMaxTimeStamp().getTime());
        }else {
            logger.info("New data found! Next job will start from " + jobStartTimeStamp.toString());
            jobCompleteNotificationListener.setFirstDateInNextJob(jobStartTimeStamp.getTime());
        }
        return result;
    }


////////////////////////////

    Map<String, ExecutionContext> getExecutionContextMap(int gridSize, ScheduledPartitionParams params) {
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





    ScheduledPartitionParams getParams() {
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

        Timestamp jobStartTimeStamp  = null;
        Timestamp jobEndTimeStamp;
        if(env.getProperty("configuredJobStartDate") !=null && configuredRunFirstRun){
            jobStartTimeStamp = getConfiguredRunAsTimestamp();
            logger.info ("configuredJobStartDate detected in configs. Commencing from " + env.getProperty("firstJobStartDate"));
            jobStartTimeStamp = getConfiguredRunAsTimestamp();
            jobEndTimeStamp =getEndTimeStamp(jobStartTimeStamp);
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    "\nBETWEEN CAST ('" + jobStartTimeStamp.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST ('" + jobEndTimeStamp.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    "\n ORDER BY " + timeStamp +" ASC " +" , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
        } else {
            try {
                jobStartTimeStamp = new Timestamp(jobExecution.getJobParameters()
                        .getDate("first_timestamp_for_next_job").getTime());
            } catch (NullPointerException ex) {}

            if(jobStartTimeStamp !=null) {
                logger.info("Next timestamp detected in DB. Commencing from " + jobStartTimeStamp.toString());
                jobEndTimeStamp = getEndTimeStamp(jobStartTimeStamp);
                sql = sql +
                        " WHERE CAST (" + timeStamp + " as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) BETWEEN CAST ('" +
                        jobStartTimeStamp.toString() +
                        "' as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) " +
                        " AND CAST ('" + jobEndTimeStamp.toString() +
                        "' as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) ) t1 " ;
            }else {
                try {
                    jobStartTimeStamp = new Timestamp(jobExecution.getJobParameters()
                            .getDate("last_timestamp_from_last_successful_job").getTime());
                } catch (NullPointerException ex) {
                }
                if (jobStartTimeStamp != null) {
                    jobStartTimeStamp = getFirstTimestampInTable(jdbcTemplate);
                    jobEndTimeStamp = getEndTimeStamp(jobStartTimeStamp);
                    sql = sql +
                            " WHERE CAST (" + timeStamp + " as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) " +
                            "> CAST ('" +
                            jobStartTimeStamp.toString() +
                            "' as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) " +
                            " AND CAST (" + timeStamp + " as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) " +
                            " <= CAST ('" + jobEndTimeStamp.toString() +
                            "' as " + env.getProperty("dbmsToJavaSqlTimestampType") + " ) ) t1";
                }
            }
        }
        if(jobStartTimeStamp ==null) {
            logger.info("No previous successful batches detected. Commencing from beginning of table");
            String tsSql = "SELECT MIN(" + timeStamp + ")  FROM " + table;
            jobStartTimeStamp = jdbcTemplate.queryForObject(tsSql,Timestamp.class);
            jobEndTimeStamp = getEndTimeStamp(jobStartTimeStamp);
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) BETWEEN CAST ('" +
                    jobStartTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST ('" + jobEndTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
        }
        logger.info ("This job SQL: " + sql);
        return (ScheduledPartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());

    }
}