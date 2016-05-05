package uk.ac.kcl.itemHandlers;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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



    private String getPartitioningLogic(String minValue, String maxValue, String minTimeStamp, String maxTimeStamp){
        String returnString;
        if(env.getProperty("useTimeStampBasedScheduling").equalsIgnoreCase("true")
                && minTimeStamp!= null && maxTimeStamp != null) {
            returnString = "WHERE " +env.getProperty("timeStamp")
                    + " BETWEEN CAST('" + minTimeStamp +
                    "' AS "+env.getProperty("dbmsToJavaSqlTimestampType")+") "
                    + " AND CAST('" + maxTimeStamp +
                    "' AS "+env.getProperty("dbmsToJavaSqlTimestampType")+") "
                    + " AND " + env.getProperty("columnToProcess")
                    + " BETWEEN '" + minValue + "' AND '" + maxValue +"'";
        }else{
            returnString = ("WHERE " + env.getProperty("columnToProcess")
                    + " BETWEEN " + minValue + " AND " + maxValue) ;
        }

        return returnString;
    }


    @Bean
    @StepScope
    @Qualifier("documentItemReader")
    public ItemReader<Document> documentItemReader(
            @Value("#{stepExecutionContext[minValue]}") String minValue,
            @Value("#{stepExecutionContext[maxValue]}") String maxValue,
            @Value("#{stepExecutionContext[min_time_stamp]}") String minTimeStamp,
            @Value("#{stepExecutionContext[max_time_stamp]}") String maxTimeStamp,
            @Qualifier("documentRowMapper")RowMapper<Document> documentRowmapper,
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {

        JdbcPagingItemReader<Document> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(jdbcDocumentSource);
        SqlPagingQueryProviderFactoryBean qp = new SqlPagingQueryProviderFactoryBean();
        qp.setSelectClause(env.getProperty("source.selectClause"));
        qp.setFromClause(env.getProperty("source.fromClause"));
        qp.setSortKey(env.getProperty("source.sortKey"));
        qp.setWhereClause(getPartitioningLogic(minValue,maxValue, minTimeStamp,maxTimeStamp));
        qp.setDataSource(jdbcDocumentSource);
        reader.setFetchSize(Integer.parseInt(env.getProperty("source.pageSize")));
        reader.setQueryProvider(qp.getObject());
        reader.setRowMapper(documentRowmapper);

        return reader;
    }


    @Bean
    @StepScope
    @Qualifier("textDocumentItemReader")
    public ItemReader<TextDocument> textDocumentItemReader(
            @Value("#{stepExecutionContext[minValue]}") String minValue,
            @Value("#{stepExecutionContext[maxValue]}") String maxValue,
            @Value("#{stepExecutionContext[min_time_stamp]}") String minTimeStamp,
            @Value("#{stepExecutionContext[max_time_stamp]}") String maxTimeStamp,
            @Qualifier("textDocumentRowMapper")RowMapper<TextDocument> documentRowmapper,
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {

        JdbcPagingItemReader<TextDocument> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(jdbcDocumentSource);
        SqlPagingQueryProviderFactoryBean qp = new SqlPagingQueryProviderFactoryBean();
        qp.setSelectClause(env.getProperty("source.selectClause"));
        qp.setFromClause(env.getProperty("source.fromClause"));
        qp.setSortKey(env.getProperty("source.sortKey"));
        qp.setWhereClause(getPartitioningLogic(minValue,maxValue, minTimeStamp,maxTimeStamp));
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
            @Value("#{stepExecutionContext[min_time_stamp]}") String minTimeStamp,
            @Value("#{stepExecutionContext[max_time_stamp]}") String maxTimeStamp,
            @Qualifier("binaryDocumentRowMapper")RowMapper<BinaryDocument> documentRowmapper,
            @Qualifier("sourceDataSource") DataSource jdbcDocumentSource) throws Exception {

        JdbcPagingItemReader<BinaryDocument> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(jdbcDocumentSource);
        SqlPagingQueryProviderFactoryBean qp = new SqlPagingQueryProviderFactoryBean();
        qp.setSelectClause(env.getProperty("source.selectClause"));
        qp.setFromClause(env.getProperty("source.fromClause"));
        qp.setSortKey(env.getProperty("source.sortKey"));
        qp.setWhereClause(getPartitioningLogic(minValue,maxValue,minTimeStamp,maxTimeStamp));
        qp.setDataSource(jdbcDocumentSource);
        reader.setFetchSize(Integer.parseInt(env.getProperty("source.pageSize")));
        reader.setQueryProvider(qp.getObject());
        reader.setRowMapper(documentRowmapper);
        return reader;
    }

    @Bean
    @Qualifier("simpleJdbcItemWriter")
    @Profile("jdbc")
    public ItemWriter<Document> simpleJdbcItemWriter(@Qualifier("targetDataSource") DataSource jdbcDocumentTarget) {
        JdbcBatchItemWriter<Document> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(env.getProperty("target.Sql"));
        writer.setDataSource(jdbcDocumentTarget);
        return writer;
    }

    @Autowired(required = false)
    @Qualifier("esDocumentWriter")
    ItemWriter<Document> esItemWriter;

    @Autowired(required = false)
    @Qualifier("simpleJdbcItemWriter")
    ItemWriter<Document> jdbcItemWriter;


    @Bean
    @Qualifier("compositeESandJdbcItemWriter")
    public ItemWriter<Document> compositeESandJdbcItemWriter() {
        CompositeItemWriter writer = new CompositeItemWriter<>();
        ArrayList<ItemWriter<Document>> delegates = new ArrayList<>();

        if(esItemWriter !=null) {
            delegates.add(esItemWriter);
        }
        if(jdbcItemWriter !=null) {
            delegates.add(jdbcItemWriter);
        }

        writer.setDelegates(delegates);
        return writer;
    }

}
