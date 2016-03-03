package uk.ac.kcl.batch;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@PropertySource("file:${TURBO_LASER}")
@Configuration
@Profile("prod")
public class Config {
    
}
