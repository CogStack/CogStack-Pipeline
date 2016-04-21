package utils;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.apache.commons.dbcp.BasicDataSource;
import javax.sql.DataSource;


/**
 * Created by rich on 21/04/16.
 */
@Component
public class BatchJobUtils {

    @Autowired
    static Environment env;

    @Autowired
    @Qualifier("targetDatasource")
    static DataSource targetDataSource;

    public static String getLastSuccessfulJobDate(){
        JdbcTemplate template = new JdbcTemplate(targetDataSource);
        String sql = "select max(start_time) AS start_time from batch_job_execution bje \n" +
                "join batch_job_instance bji on bje.job_instance_id = bji.job_instance_id \n" +
                "where bje.exit_code = 'COMPLETED' and bji.job_name = '" + env.getProperty("jobName") + "'";

        String startTime = (String)template.queryForObject(sql, String.class);
        return startTime;



    }
}
