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
package uk.ac.kcl.listeners;

//import io.bluecell.data.JDBCDocumentSource;
//import io.bluecell.data.JDBCDocumentTarget;
import uk.ac.kcl.model.BinaryDocument;
import java.util.List;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class JobCompleteNotificationListener extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(JobCompleteNotificationListener.class);

//        @Autowired
//	public JDBCDocumentTarget jdbcDocumentTarget;

	private DataSource targetDataSource;        
        
        @Autowired
        public void setDataSource(@Qualifier("targetDataSource") DataSource targetDataSource){
            this.targetDataSource = targetDataSource;
        }
                
                
        @Autowired
        Environment env;
        
        @Autowired
        @Qualifier("validationQueryRowMapper")        
        RowMapper<BinaryDocument> rowmapper; 


	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("!!! JOB FINISHED! Time to verify the results");

                            JdbcTemplate jdbcTemplate = new JdbcTemplate(targetDataSource);
			List<BinaryDocument> results = 
                                jdbcTemplate
                                .query(env.getProperty("target.validationQuery"), rowmapper);

			for (BinaryDocument doc : results) {
				log.info("Found <" + doc.getPrimaryKeyFieldValue() + "> in the database.");
			}

		}
	}
}