package uk.ac.kcl.itemHandlers;

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
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.kcl.model.BinaryDocument;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by rich on 15/04/16.
 */
@Configuration
public class ItemHandlers {

    @Resource
    Environment env;
    @Bean
    @StepScope
    @Qualifier("simpleItemReader")
    public ItemReader<BinaryDocument> simpleItemReader(
            @Value("#{stepExecutionContext[minValue]}") String minValue,
            @Value("#{stepExecutionContext[maxValue]}") String maxValue,
            @Qualifier("binaryDocumentRowMapper")RowMapper<BinaryDocument> documentRowmapper,
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {

        JdbcPagingItemReader<BinaryDocument> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(jdbcDocumentSource);

        SqlPagingQueryProviderFactoryBean qp = new SqlPagingQueryProviderFactoryBean();
        qp.setSelectClause(env.getProperty("source.selectClause"));
        qp.setFromClause(env.getProperty("source.fromClause"));
        qp.setSortKey(env.getProperty("source.sortKey"));
        qp.setWhereClause("WHERE " + env.getProperty("columntoPartition") + " BETWEEN " + minValue + " AND " + maxValue) ;
        qp.setDataSource(jdbcDocumentSource);
        reader.setFetchSize(Integer.parseInt(env.getProperty("source.pageSize")));


        reader.setQueryProvider(qp.getObject());
        reader.setRowMapper(documentRowmapper);

        return reader;
    }

    @Bean
    @Qualifier("simpleItemWriter")
    public ItemWriter<BinaryDocument> simpleItemWriter(@Qualifier("targetDataSource") DataSource jdbcDocumentTarget) {
        JdbcBatchItemWriter<BinaryDocument> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(env.getProperty("target.Sql"));
        writer.setDataSource(jdbcDocumentTarget);
        return writer;
    }
}
