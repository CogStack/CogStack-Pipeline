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

import gate.util.GateException;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.kcl.model.MultilineDocument;
import uk.ac.kcl.rowmappers.MultiRowDocumentRowMapper;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Configuration
@Profile("dBLineFixer")
@PropertySource("file:${TURBO_LASER}/dBLineFixer.conf")
public class DbLineFixerConfiguration {
    @Resource
    Environment env;
    /* 
    
    
    
    *******************************************Multiline JOB
    
    
    
    */

    @Bean
    @StepScope
    @Qualifier("dBLineFixerItemReader")
    public ItemReader<MultilineDocument> dBLineFixerItemReader(
            @Value("#{stepExecutionContext[minValue]}") String minValue,
            @Value("#{stepExecutionContext[maxValue]}") String maxValue,
            @Qualifier("multiRowDocumentRowmapper")RowMapper<MultilineDocument> multiRowDocumentRowmapper,
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {
        JdbcPagingItemReader<MultilineDocument> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(jdbcDocumentSource);
        SqlPagingQueryProviderFactoryBean qp = new SqlPagingQueryProviderFactoryBean();
        qp.setSelectClause(env.getProperty("source.selectClause"));
        qp.setFromClause(env.getProperty("source.fromClause"));
        qp.setSortKey(env.getProperty("source.sortKey"));
        qp.setWhereClause("WHERE " + env.getProperty("columntoPartition") + " BETWEEN " + minValue + " AND " + maxValue) ;
        qp.setDataSource(jdbcDocumentSource);
        reader.setFetchSize(Integer.parseInt(env.getProperty("source.pageSize")));


        reader.setQueryProvider(qp.getObject());
        reader.setRowMapper(multiRowDocumentRowmapper);

        return reader;
    }


    @Bean
    @Qualifier("dBLineFixerItemWriter")
    public ItemWriter<MultilineDocument> dBLineFixerItemWriter(
            @Qualifier("targetDataSource") DataSource jdbcDocumentTarget) {
        JdbcBatchItemWriter<MultilineDocument> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(env.getProperty("target.Sql"));
        writer.setDataSource(jdbcDocumentTarget);
        return writer;
    }

    @Bean
    @Qualifier("multiRowDocumentRowmapper")
    public RowMapper multiRowDocumentRowmapper(
            @Qualifier("sourceDataSource") DataSource ds) {
        MultiRowDocumentRowMapper mapper = new MultiRowDocumentRowMapper(ds);
        return mapper;
    }


    @Bean
    public Step dBLineFixerSlaveStep(
            @Qualifier("dBLineFixerItemReader") ItemReader<MultilineDocument> reader,
            @Qualifier("dBLineFixerItemWriter")  ItemWriter<MultilineDocument> writer,
            @Qualifier("slaveTaskExecutor")TaskExecutor taskExecutor,
            StepBuilderFactory stepBuilderFactory
    ) {
        Step step = stepBuilderFactory.get("dBLineFixerSlaveStep")
                .<MultilineDocument, MultilineDocument> chunk(
                        Integer.parseInt(env.getProperty("chunkSize")))
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skipLimit(Integer.parseInt(env.getProperty("skipLimit")))
                .skip(Exception.class)
                .taskExecutor(taskExecutor)
                .build();

        return step;
    }
}
