package uk.ac.kcl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import uk.ac.kcl.batch.ScheduledJobConfiguration;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
public class Main {
    
    public static void main(String[] args) {
        SimpleCommandLinePropertySource ps = new SimpleCommandLinePropertySource(args);        
        if (ps.getProperty("nonOptionArgs").contains("scheduled")) {
            
            @SuppressWarnings("resource")
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
            ctx.getEnvironment().getPropertySources().addFirst(ps);
            ctx.register(ScheduledJobConfiguration.class);
            ctx.refresh();            
            ScheduledJobConfiguration job = ctx.getBean(ScheduledJobConfiguration.class);
        } else {
            try {
                CommandLineJobRunner.main(args);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
