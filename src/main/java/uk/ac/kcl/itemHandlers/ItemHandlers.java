package uk.ac.kcl.itemHandlers;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.TextDocument;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;

/**
 * Created by rich on 15/04/16.
 */
@Configuration
public class ItemHandlers {

    @Resource
    Environment env;




    @Bean
    @StepScope
    @Qualifier("textDocumentItemReader")
    public ItemReader<TextDocument> textDocumentItemReader(
            @Value("#{stepExecutionContext[minValue]}") String minValue,
            @Value("#{stepExecutionContext[maxValue]}") String maxValue,
            @Qualifier("textDocumentRowMapper")RowMapper<TextDocument> documentRowmapper,
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {

        JdbcPagingItemReader<TextDocument> reader = new JdbcPagingItemReader<>();
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
    @StepScope
    @Qualifier("binaryDocumentItemReader")
    public ItemReader<BinaryDocument> binaryDocumentItemReader(
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
    @Qualifier("simpleJdbcItemWriter")
    public ItemWriter<Document> simpleJdbcItemWriter(@Qualifier("targetDataSource") DataSource jdbcDocumentTarget) {
        JdbcBatchItemWriter<Document> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(env.getProperty("target.Sql"));
        writer.setDataSource(jdbcDocumentTarget);
        return writer;
    }

    @Bean
    @Qualifier("compositeESandJdbcItemWriter")
    public ItemWriter<Document> compositeESandJdbcItemWriter(
            @Qualifier("esDocumentWriter") ItemWriter<Document> esItemWriter,
            @Qualifier("simpleJdbcItemWriter") ItemWriter<Document> jdbcItemWriter) {
        CompositeItemWriter writer = new CompositeItemWriter<>();
        ArrayList<ItemWriter<Document>> delegates = new ArrayList<>();
        delegates.add(esItemWriter);
        delegates.add(jdbcItemWriter);
        writer.setDelegates(delegates);
        return writer;

    }
}
