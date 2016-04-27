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
import java.util.Date;
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
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.listeners.JobCompleteNotificationListener;
import uk.ac.kcl.model.PartitionParams;
import uk.ac.kcl.rowmappers.PartitionParamsRowMapper;
import uk.ac.kcl.utils.BatchJobUtils;

/**
 * Simple minded partitioner for a range of values of a column in a database
 * table. Works best if the values are uniformly distributed (e.g.
 * auto-generated primary key values).
 *
 * @author Dave Syer
 *
 */
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
		Timestamp lastGoodJob;
		Timestamp processingPeriod;
		String sql;
		if(env.getProperty("firstJobStartDate") !=null){
			lastGoodJob = null;
			Timestamp earliestRecord = new Timestamp(Long.valueOf(env.getProperty("firstJobStartDate")));
			processingPeriod = new Timestamp(earliestRecord.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
			sql =	" SELECT "   +
					" MAX(" + column + ") AS max_id , " +
					" MIN(" + column + ") AS min_id , " +
					" MAX(" + timeStamp + ") AS max_time_stamp , " +
					" MIN(" + timeStamp + ") AS min_time_stamp  " +
					" FROM ( " +
					" SELECT " +
					//batchJobUtils.cleanSqlString(env.getProperty("partitionerPreFieldsSQL")) +
					" " + column + ", " + timeStamp +
					" FROM " + table + " " +
					" WHERE " + timeStamp + " BETWEEN '" + earliestRecord.toString() +
					"' AND '" + processingPeriod.toString() + "'" +
					" ORDER BY " + timeStamp +" ASC " +" , " + column +" ASC " +
					//batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
					" ) t1" ;
		} else if(batchJobUtils.getLastSuccessfulRecordTimestamp() != null) {
			lastGoodJob = batchJobUtils.getLastSuccessfulRecordTimestamp();
			processingPeriod = new Timestamp(lastGoodJob.getTime() + Long.valueOf(env.getProperty("processingPeriod")));
			sql =	" SELECT "   +
					" MAX(" + column + ") AS max_id , " +
					" MIN(" + column + ") AS min_id , " +
					" MAX(" + timeStamp + ") AS max_time_stamp , " +
					" MIN(" + timeStamp + ") AS min_time_stamp  " +
					" FROM ( " +
					" SELECT " +
					//batchJobUtils.cleanSqlString(env.getProperty("partitionerPreFieldsSQL")) +
					" " + column + ", " + timeStamp +
					" FROM " + table + " " +
					" WHERE " + timeStamp + " BETWEEN '" + lastGoodJob.toString() +
					"' AND '" + processingPeriod.toString() + "'" +
					" ORDER BY " + timeStamp  +" ASC " + " , " + column +" ASC " +
					//batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
					" ) t1" ;
		}else{
			lastGoodJob = null;
			String tsSql = "SELECT MIN(" + timeStamp + ")  FROM " + table;

			Timestamp earliestRecord = jdbcTemplate.queryForObject(tsSql,Timestamp.class);
			String test = env.getProperty("processingPeriod");
			processingPeriod = new Timestamp(Long.parseLong(test));
			processingPeriod.setTime(earliestRecord.getTime() + processingPeriod.getTime());
			sql =	" SELECT "   +
					" MAX(" + column + ") AS max_id , " +
					" MIN(" + column + ") AS min_id , " +
					" MAX(" + timeStamp + ") AS max_time_stamp , " +
					" MIN(" + timeStamp + ") AS min_time_stamp  " +
					" FROM ( " +
					" SELECT " +
					//batchJobUtils.cleanSqlString(env.getProperty("partitionerPreFieldsSQL")) +
					" " + column + ", " + timeStamp +
					" FROM " + table + " " +
					" WHERE " + timeStamp + " BETWEEN '" + earliestRecord.toString() +
					"' AND '" + processingPeriod.toString() + "'" +
					" ORDER BY " + timeStamp  +" ASC " + " , " + column +" ASC " +
					//batchJobUtils.cleanSqlString(env.getProperty("partitionerPostOrderByClause")) +
					" ) t1" ;
		}
		logger.info ("This job SQL: " + sql);
		PartitionParams params = (PartitionParams) jdbcTemplate.queryForObject(
				sql, new PartitionParamsRowMapper());
		jobCompleteNotificationListener.setLastDateInthisJob(String.valueOf(params.getMaxTimeStamp()));
		Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
		long targetSize = (params.getMaxId() - params.getMinId()) / gridSize + 1;
		long number = 0;
		long start = params.getMinId();
		long end = start + targetSize - 1;
		for(int i=0; i<gridSize; i++) {
			ExecutionContext value = new ExecutionContext();
			result.put("partition" + (i+1), value);
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