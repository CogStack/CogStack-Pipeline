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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;


import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
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
public class ColumnRangePartitioner implements Partitioner {

	@Autowired
	@Qualifier("sourceDataSource")
	DataSource sourceDataSource;

	@Autowired
	Environment env;

	@Autowired
	BatchJobUtils batchJobUtils;

	@PostConstruct
	public void init(){
		setColumn(env.getProperty("columntoPartition"));
		setTable(env.getProperty("tableToPartition"));
		setDataSource(sourceDataSource);

	}



	private JdbcOperations jdbcTemplate;

	private String table;

	private String column;

	public void setTable(String table) {
		this.table = table;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {

		String lastGoodJob = batchJobUtils.getLastSuccessfulJobDate();
		long min = jdbcTemplate.queryForObject("SELECT MIN(" + column + ") from " + table, Long.class);
		long max = jdbcTemplate.queryForObject("SELECT MAX(" + column + ") from " + table, Long.class);
		long targetSize = (max - min) / gridSize + 1;

		Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
		long number = 0;
		long start = min;
		long end = start + targetSize - 1;

		while (start <= max) {
			ExecutionContext value = new ExecutionContext();
			result.put("partition" + number, value);

			if (end >= max) {
				end = max;
			}
			value.putLong("minValue", start);
			value.putLong("maxValue", end);
			value.putString("previousSuccessfulJobStartTime",lastGoodJob);
			start += targetSize;
			end += targetSize;
			number++;
		}

		return result;
	}
}