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


@Service("realtimePKOnlyRangePartitioner")
@ComponentScan("uk.ac.kcl.listeners")
@org.springframework.context.annotation.Profile("primaryKeyPartition")
public class RealtimePKRangePartitioner extends AbstractRealTimeRangePartitioner implements Partitioner {

    final static Logger logger = LoggerFactory.getLogger(RealtimePKRangePartitioner.class);

    public Map<String, ExecutionContext> getExecutionContextMap(int gridSize, Map<String, ExecutionContext> result) {
        Timestamp jobStartTimeStamp  = null;
        try {
            jobStartTimeStamp = new Timestamp(jobExecution.getJobParameters()
                    .getDate("last_timestamp_from_last_successful_job").getTime());
        }catch(NullPointerException ex){}
        ;
        ScheduledPartitionParams params = getParams(jobStartTimeStamp);
        if(noRecordsFoundInProcessingPeriod(params)){
            return handleNoNewRecords(result, jobStartTimeStamp);
        } else if ((params.getMaxId() -params.getMinId()) < (long) gridSize) {
            long partitionCount = (params.getMaxId() -params.getMaxId());
            logger.info("There are fewer new records than the grid size. Expect only " + partitionCount+ "partitions this execution") ;
            for(long i = 0;i<(partitionCount);i++) {
                ExecutionContext value = new ExecutionContext();
                result.put("partition" + (i + 1L), value);
                value.putLong("minValue", (params.getMinId()+1L+i) );
                value.putLong("maxValue", (params.getMinId()+1L+i) );
            }
        } else {
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
        if (params.getMaxTimeStamp() !=null){
            jobCompleteNotificationListener.setLastDateInthisJob(params.getMaxTimeStamp().getTime());
        }
        logger.info("partitioning complete");
        return result;
    }


    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
            logger.info("Commencing PK only partition");
            result = getExecutionContextMap(gridSize, result);
        if(configuredRunFirstRun){
            configuredRunFirstRun =false;
        }
        return result;
    }


    private Map<String, ExecutionContext> handleNoNewRecords(Map<String, ExecutionContext> result, Timestamp jobStartTimeStamp) {
        if(configuredRunFirstRun) {
            logger.info("No new data found from configured start time " + String.valueOf(jobStartTimeStamp.toString()));
            jobCompleteNotificationListener.setLastDateInthisJob(jobStartTimeStamp.getTime());
        }else {
            logger.info("Database appears to be synched as far as " + jobStartTimeStamp.toString() + ". " +
                    "Checking again on next run");
            jobCompleteNotificationListener.setLastDateInthisJob(jobStartTimeStamp.getTime());
        }
        return result;
    }


    private ScheduledPartitionParams getParams(Timestamp startTimeStamp) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        String sql = "\n SELECT "   +
                " MAX(" + column + ") AS max_id , \n" +
                " MIN(" + column + ") AS min_id , \n" +
                " MAX(" + timeStamp + ") AS max_time_stamp , \n" +
                " MIN(" + timeStamp + ") AS min_time_stamp  \n" +
                " FROM " + table ;
        if(env.getProperty("firstJobStartDate") !=null && configuredRunFirstRun){
            logger.info ("firstJobStartDate detected in configs. Commencing from " + env.getProperty("firstJobStartDate"));
            Timestamp firstRunAsTimestamp = getConfiguredRunAsTimestamp();
            sql = sql + " WHERE " + timeStamp + " >= CAST ('" + firstRunAsTimestamp.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " ;
        } else if(startTimeStamp != null) {
            logger.info ("last successful batch retrieved from job repository. Commencing from after " + startTimeStamp.toString());
            sql =	sql +
                    "\n WHERE CAST (" + timeStamp + " as "+
                    env.getProperty("dbmsToJavaSqlTimestampType")+
                    " ) > CAST ('" + startTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) ";
        }else if(jobExecution.getJobParameters().getString("first_run_of_job").equalsIgnoreCase("true")){
            startTimeStamp = getFirstTimestampInTable(jdbcTemplate);
            logger.info ("No previous successful batches detected. Commencing from first timestamp: " + startTimeStamp.toString());
        }else{
            logger.error("unable to determine partition requirement");
        }
        logger.info ("This job SQL: " + sql);
        return (ScheduledPartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());
    }

}