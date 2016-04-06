package uk.ac.kcl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import uk.ac.kcl.batch.ScheduledJobConfiguration;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
public class Main {

    public static void main(String[] args) {
        if (args[3].equals("schedule")) {
            ApplicationContext context = SpringApplication
                    .run(ScheduledJobConfiguration.class);
        } else {
            try {
                CommandLineJobRunner.main(args);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
