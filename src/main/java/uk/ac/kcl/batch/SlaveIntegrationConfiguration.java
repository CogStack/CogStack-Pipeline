package uk.ac.kcl.batch;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Profile("slave")
@ImportResource("classpath:spring-slave.xml")
@Configuration
class SlaveIntegrationConfiguration {
    
}
