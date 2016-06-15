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

    private final static Logger logger = LoggerFactory.getLogger(RealtimePKRangePartitioner.class);

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result;
        Timestamp startTimestamp = getStartTimeStampIfConfiguredOrFirstRun();
        ScheduledPartitionParams params = getParams(startTimestamp);
        if(noRecordsFoundInProcessingPeriod(params)){
            result = handleNoNewRecords(getLastTimestampFromLastSuccessfulJob());
        }else{
            result = getExecutionContextMap(gridSize,params);
        }
        if(firstRun){
            firstRun =false;
        }
        return result;
    }


    private Map<String, ExecutionContext> getExecutionContextMap(int gridSize, ScheduledPartitionParams params) {
        logger.info("Commencing PK only partition");
        return getMap(gridSize, params);
    }



    private Map<String, ExecutionContext> handleNoNewRecords (Timestamp startTimeStamp) {
        if(firstRun) {
            logger.info("No new data found from configured start time " + startTimeStamp.toString());
            jobCompleteNotificationListener.setLastDateInthisJob(startTimeStamp.getTime());
        }else {
            logger.info("Database appears to be synched as far as " + startTimeStamp.toString() + ". " +
                    "Checking again on next run");
            jobCompleteNotificationListener.setLastDateInthisJob(startTimeStamp.getTime());
        }
        return new HashMap<>();
    }


    private ScheduledPartitionParams getParams(Timestamp startTimeStamp) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDataSource);
        String sql = "\n SELECT "   +
                " MAX(" + column + ") AS max_id , \n" +
                " MIN(" + column + ") AS min_id , \n" +
                " MAX(" + timeStamp + ") AS max_time_stamp , \n" +
                " MIN(" + timeStamp + ") AS min_time_stamp  \n" +
                " FROM " + table ;
        if(configuredFirstRunTimestamp !=null && firstRun){
            sql = sql + " WHERE " + timeStamp + " >= CAST ('" + startTimeStamp.toString() +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " ;
        }else if(startTimeStamp == null) {
            Timestamp newStartTimeStamp = getLastTimestampFromLastSuccessfulJob();
            logger.info ("Commencing from after " +
                    newStartTimeStamp.toString());
            sql =	sql +
                    "\n WHERE CAST (" + timeStamp + " as "+
                    env.getProperty("dbmsToJavaSqlTimestampType")+
                    " ) > CAST ('" + newStartTimeStamp.toString()  +
                    "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) ";
        }else if(firstRun) {
        //no new SQL required - process all data for first run
            logger.debug("first run");
        }else{
            throw new RuntimeException("unable to determine partition requirement");
        }
        logger.info ("This job SQL: " + sql);
        return (ScheduledPartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());
    }

}