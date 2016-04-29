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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.ac.kcl.rowmappers.PartitionParamsRowMapper;
import uk.ac.kcl.utils.BatchJobUtils;


@Service("columnRangePartitioner")
@ComponentScan("uk.ac.kcl.listeners")
public class ColumnRangePartitioner implements Partitioner {

    final static Logger logger = LoggerFactory.getLogger(ColumnRangePartitioner.class);

    @Autowired
    @Qualifier("sourceDataSource")
    DataSource sourceDataSource;

    @Autowired
    Environment env;

    @Autowired
    JobCompleteNotificationListener jobCompleteNotificationListener;

    private String timeStamp;
    private boolean firstRun = true;

    @PostConstruct
    public void init(){
        setColumn(env.getProperty("columntoPartition"));
        setTable(env.getProperty("tableToPartition"));
        setTimeStampColumnName(env.getProperty("timeStamp"));
        setDataSource(sourceDataSource);
    }

    @Autowired
    BatchJobUtils batchJobUtils;

    private JdbcTemplate jdbcTemplate;
    Timestamp lastGoodJob;
    Timestamp processingPeriod;


    private String table;

    private String column;

    public void setTable(String table) {
        this.table = table;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setTimeStampColumnName(String timeStamp) {this.timeStamp = timeStamp;}

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        PartitionParams params = getPartitionParams();
        Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
        if(noRecordsFoundInProcessingPeriod(params)) {
            Timestamp newestTimestampInDB = batchJobUtils.checkForNewRecordsBeyondConfiguredProcessingPeriod(table, lastGoodJob, timeStamp);
            if (newestTimestampInDB == null) {
                jobCompleteNotificationListener.setLastDateInthisJob(String.valueOf(lastGoodJob.getTime()));
                logger.info("Database appears to be synched as far as " + String.valueOf(lastGoodJob.toString())
                        + "Checking again on next job" );
                return result;
            } else {
                logger.info("New data found! Next job will synch as far as " + String.valueOf(newestTimestampInDB.toString()));
                jobCompleteNotificationListener.setLastDateInthisJob(String.valueOf(newestTimestampInDB.getTime()));
            }
            return result;
        }else {
            logger.info("Database not yet synched. Synching as far as  "
                    + params.getMaxTimeStamp().toString() +" this job");
            jobCompleteNotificationListener.setLastDateInthisJob(String.valueOf(params.getMaxTimeStamp().getTime()));

            long targetSize = (params.getMaxId() - params.getMinId()) / gridSize + 1;
            long number = 0;
            long start = params.getMinId();
            long end = start + targetSize - 1;
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext value = new ExecutionContext();
                result.put("partition" + (i + 1), value);
                value.putLong("minValue", start);
                value.putLong("maxValue", end);
                value.put("min_time_stamp", params.getMinTimeStamp().toString());
                value.put("max_time_stamp", params.getMaxTimeStamp().toString());
                start += targetSize;
                end += targetSize;
                number++;
            }
            return result;
        }
    }

    private boolean noRecordsFoundInProcessingPeriod(PartitionParams partitionParams){
        if(partitionParams.getMinTimeStamp() == null){
            return true;
        }else{
            return false;
        }
    }

    private PartitionParams getPartitionParams() {

        String sql = " SELECT "   +
                " MAX(" + column + ") AS max_id , " +
                " MIN(" + column + ") AS min_id , " +
                " MAX(" + timeStamp + ") AS max_time_stamp , " +
                " MIN(" + timeStamp + ") AS min_time_stamp  " +
                " FROM ( " +
                " SELECT " +
                batchJobUtils.cleanSqlString(env.getProperty("partitionerPreFieldsSQL")) +
                " " + column + ", " + timeStamp +
                " FROM " + table + " ";
        if(env.getProperty("firstJobStartDate") !=null && firstRun){
            logger.info ("firstJobStartDate detected in configs. Commencing from " + env.getProperty("firstJobStartDate"));
            lastGoodJob = null;
            Timestamp earliestRecord = new Timestamp(Long.valueOf(env.getProperty("firstJobStartDate")));
            processingPeriod = new Timestamp(earliestRecord.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) > CAST ('" + earliestRecord.toString() + "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) <= CAST ('" + processingPeriod.toString() + "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " ORDER BY " + timeStamp +" ASC " +" , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
            firstRun = false;
        } else if(batchJobUtils.getLastSuccessfulRecordTimestamp() != null) {
            lastGoodJob = batchJobUtils.getLastSuccessfulRecordTimestamp();
            logger.info ("last successful batch retrieved from job repository. Commencing from " + lastGoodJob.toString());
            processingPeriod = new Timestamp(lastGoodJob.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) > CAST ('" + lastGoodJob.toString()  + "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST ("  + timeStamp +  " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) <= CAST ('" + processingPeriod.toString() + "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " ORDER BY " + timeStamp  +" ASC " + " , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
        }else{
            lastGoodJob = null;
            String tsSql = "SELECT MIN(" + timeStamp + ")  FROM " + table;
            Timestamp earliestRecord = jdbcTemplate.queryForObject(tsSql,Timestamp.class);
            logger.info ("No previous successful batches detected. Commencing from first timestamp: " + earliestRecord.toString());
            String test = env.getProperty("processingPeriod");
            processingPeriod = new Timestamp(Long.parseLong(test));
            processingPeriod.setTime(earliestRecord.getTime() + processingPeriod.getTime());
            sql =	sql +
                    " WHERE CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) > CAST ('" + earliestRecord.toString()  + "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " AND CAST (" + timeStamp + " as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) <= CAST ('" + processingPeriod.toString()  + "' as "+env.getProperty("dbmsToJavaSqlTimestampType")+" ) " +
                    " ORDER BY " + timeStamp  +" ASC " + " , " + column +" ASC " +
                    batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
                    " ) t1" ;
        }
        logger.info ("This job SQL: " + sql);
        return (PartitionParams) jdbcTemplate.queryForObject(
                sql, new PartitionParamsRowMapper());
    }
}