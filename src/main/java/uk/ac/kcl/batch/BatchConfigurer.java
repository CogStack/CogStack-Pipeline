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
package uk.ac.kcl.batch;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * @author rich
 */
@Configuration
@EnableBatchProcessing
@PropertySource("file:${gate_cog}")
public class BatchConfigurer extends DefaultBatchConfigurer{

    
//	@Autowired
//	private Environment env;
// 
//	@Bean
//	public DataSource dataSource(){
//		try {
//			InitialContext initialContext = new InitialContext();
//			return (DataSource) initialContext.lookup(env.getProperty("datasource.jndi"));
//		} catch (NamingException e) {
//			throw new RuntimeException("JNDI lookup failed.",e);
//		}
//	}
// 
//	public JobRepository getJobRepository() throws Exception {
//		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//		factory.setDataSource(dataSource());
//		factory.setTransactionManager(getTransactionManager());
//		factory.afterPropertiesSet();
//		return  (JobRepository) factory.getObject();
//	}
// 
//	public PlatformTransactionManager getTransactionManager() throws Exception {
//		return new WebSphereUowTransactionManager();
//	}
// 
//	public JobLauncher getJobLauncher() throws Exception {
//		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
//		jobLauncher.setJobRepository(getJobRepository());
//		jobLauncher.afterPropertiesSet();
//		return jobLauncher;
//	}    
//    
    

}
