package uk.ac.kcl.batch;

import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.ac.kcl.scheduling.Scheduler;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Configuration
@Import(JobConfiguration.class)
@EnableScheduling
public class ScheduledJobConfiguration {

    //required to process placeholder values in annotations, e.g. scheduler cron
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Autowired
    Environment env;

    @Bean
    public Scheduler scheduler() {

        Scheduler sched ;
        String[] nonOptionArgs = env.getProperty("nonOptionArgs", String[].class);
        ArrayList<String> arr = new ArrayList(Arrays.asList(nonOptionArgs));

        if (arr.contains("existing")) {
            System.out.println("HEEEEEEEEEEEEERRE" + nonOptionArgs[1]);
            System.out.println("SET EXISTING IS NOW TRUE OR ");
            sched = new Scheduler(true);
        }else{
            sched = new Scheduler(false);
        }
        return sched;
    }
}
