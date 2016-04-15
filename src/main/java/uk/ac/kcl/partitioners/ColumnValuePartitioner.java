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

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.kcl.rowmappers.ColumnKeyRowMapper;


import javax.sql.DataSource;
import java.util.*;


public class ColumnValuePartitioner implements Partitioner {

	private JdbcOperations jdbcTemplate;

	private String table;

	private String column;

	/**
	 * The name of the SQL table the data are in.
	 *
	 * @param table the name of the table
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * The name of the column to partition.
	 *
	 * @param column the column name.
	 */
	public void setColumn(String column) {
		this.column = column;
	}

	/**
	 * The data source for connecting to the database.
	 *
	 * @param dataSource a {@link DataSource}
	 */
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Partition a database table assuming that the data in the column specified
	 * are uniformly distributed. The execution context values will have keys
	 * <code>minValue</code> and <code>maxValue</code> specifying the range of
	 * values to consider in each partition.
	 *
	 * @see Partitioner#partition(int)
	 */
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {

		String sql = "SELECT " + column + " FROM " + table;
        List<String> keyAR = jdbcTemplate.query(sql,new ColumnKeyRowMapper());
		Iterator<String> keysIter = keyAR.iterator();
		Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
		long keyCount = keyAR.size();
		System.out.println("Total Keys = " + keyCount);
		long targetSize = keyCount / gridSize +1;
		System.out.println("keys per Partition = " + targetSize);
		int partitionIndex = 1;
		while(keysIter.hasNext()){
			ExecutionContext value = new ExecutionContext();
			System.out.println("new partition " + partitionIndex);
			result.put("partition" + partitionIndex, value);
			ArrayList<String> arr = new ArrayList<>();
			for (int i = 0; i < targetSize; i++) {
				arr.add(keysIter.next());
				if (!keysIter.hasNext()) {
					break;
				}

			}
			partitionIndex++;
			System.out.println("partition contains " + Arrays.deepToString(arr.toArray()));
			value.put("keyArray",Arrays.deepToString(arr.toArray()));
		}




		return result;
	}
}