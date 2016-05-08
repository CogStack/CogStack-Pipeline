/* 
 * Copyright 2016 King's College London, Richard Jackson <richgjackson@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.kcl;

import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;
import uk.ac.kcl.scheduling.SingleJobLauncher;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
public class Main {
    
    public static void main(String[] args) {
        SimpleCommandLinePropertySource ps = new SimpleCommandLinePropertySource(args);
        @SuppressWarnings("resource")
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addFirst(ps);
        if (ps.getProperty("nonOptionArgs").contains("scheduled")) {
            ctx.register(ScheduledJobLauncher.class);
            ctx.refresh();
        } else if(ps.getProperty("nonOptionArgs").contains("single")){
            ctx.register(SingleJobLauncher.class);
            ctx.refresh();
            SingleJobLauncher launcher = ctx.getBean(SingleJobLauncher.class);
            launcher.launchJob();
        } else if (ps.getProperty("nonOptionArgs").contains("slave")){
            ctx.register(JobConfiguration.class);
            ctx.refresh();
        }
        else{
            try {
                CommandLineJobRunner.main(args);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
