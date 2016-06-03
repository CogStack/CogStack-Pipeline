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
import org.springframework.core.env.*;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;
import uk.ac.kcl.scheduling.SingleJobLauncher;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
public class Main {

    public static void main(String[] args) {

        File folder = new File(args[0]);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if(listOfFiles[i].getName().endsWith(".properties")){
                    System.out.println("Properties sile found:" + listOfFiles[i].getName() +
                            ". Attempting to launch application context");
                    Properties properties = new Properties();
                    InputStream input = null;

                    try{
                        input = new FileInputStream(listOfFiles[i]);
                        properties.load(input);
                        Map<String,Object> map = new HashMap<>();
                        properties.forEach((k,v)->{
                            map.put(k.toString(),v);
                        });
                        ConfigurableEnvironment environment = new StandardEnvironment();
                        MutablePropertySources propertySources = environment.getPropertySources();
                        propertySources.addFirst(new MapPropertySource(listOfFiles[i].getName(),map));
                        @SuppressWarnings("resource")
                        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
                        ctx.setEnvironment(environment);
                        if (properties.getProperty("useScheduling").equalsIgnoreCase("true")) {
                            ctx.register(ScheduledJobLauncher.class);
                            ctx.refresh();
                        } else if(properties.getProperty("useScheduling").equalsIgnoreCase("false")) {
                            ctx.register(SingleJobLauncher.class);
                            ctx.refresh();
                            SingleJobLauncher launcher = ctx.getBean(SingleJobLauncher.class);
                            launcher.launchJob();
                        } else if (properties.getProperty("useScheduling").equalsIgnoreCase("slave")) {
                            ctx.register(JobConfiguration.class);
                            ctx.refresh();
                        }else {
                            throw new RuntimeException("useScheduling not configured. Must be true, false or slave");
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
