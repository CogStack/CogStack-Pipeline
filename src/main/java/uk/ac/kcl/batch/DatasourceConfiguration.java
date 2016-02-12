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

//import io.bluecell.data.JDBCDocumentSource;
//import io.bluecell.data.JDBCDocumentTarget;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 *
 * @author rich
 */
@Component
public class DatasourceConfiguration {

    @Autowired
    Environment env;

//    @Bean(destroyMethod = "close")
//    @Primary
//    @Qualifier("sourceDataSource")
//    public DataSource sourceDataSource() {
//        BasicDataSource ds = new BasicDataSource();
//        ds.setDriverClassName(env.getProperty("source.Driver"));
//        ds.setUrl(env.getProperty("source.JdbcPath"));
//        ds.setUsername(env.getProperty("source.username"));
//        ds.setPassword(env.getProperty("source.password"));        
//        return ds;
//    }
//
//    @Bean(destroyMethod = "close")
//    @Qualifier("targetDataSource")
//    public DataSource targetDataSource() {
//        BasicDataSource ds = new BasicDataSource();
//        ds.setDriverClassName(env.getProperty("target.Driver"));
//        ds.setUrl(env.getProperty("target.JdbcPath"));
//        ds.setUsername(env.getProperty("target.username"));
//        ds.setPassword(env.getProperty("target.password"));                
//        return ds;
//    }
//    @Bean
//    public JDBCDocumentTarget jdbcTargetDocumentFinder(){
//        return new JDBCDocumentTarget();
//    }
//    
//    @Bean
//    public JDBCDocumentSource jdbcSourceDocumentFinder(){
//        return new JDBCDocumentSource();
//    }
    
    
}
