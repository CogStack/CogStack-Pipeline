package uk.ac.kcl.batch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
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

    @Bean
    public Scheduler scheduler() {
        return new Scheduler();
    }
}
