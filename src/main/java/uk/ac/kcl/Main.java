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

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import uk.ac.kcl.batch.JobConfiguration;
import uk.ac.kcl.scheduling.ScheduledJobLauncher;
import uk.ac.kcl.scheduling.SingleJobLauncher;
import uk.ac.kcl.utils.TcpHelper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
public class Main {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        File folder = new File(args[0]);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (listOfFile.getName().endsWith(".properties")) {
                    LOG.info("Properties sile found:" + listOfFile.getName() +
                            ". Attempting to launch application context");
                    Properties properties = new Properties();
                    InputStream input;
                    try {
                        input = new FileInputStream(listOfFile);
                        properties.load(input);

                        // check whether any partitioning scheme has been selected
                        // TODO: move to a module performing input parameters validation
                        if (properties.containsKey("spring.profiles.active")) {
                            String activeProfiles = properties.getProperty("spring.profiles.active");
                            if (!activeProfiles.contains("localPartitioning") && !activeProfiles.contains("remotePartitioning")) {
                                activeProfiles += ",localPartitioning";
                                properties.replace("spring.profiles.active", activeProfiles);
                                LOG.info("No partitioning scheme specified in the active profiles. Using 'localPartitioning' by default");
                            }
                        }

                        // TODO: need a proper way to validate input properties specified by user
                        if(properties.getProperty("globalSocketTimeout")!=null){
                            TcpHelper.setSocketTimeout(Integer.valueOf(properties.getProperty("globalSocketTimeout")));
                        }
                        Map<String, Object> map = new HashMap<>();
                        properties.forEach((k, v) -> {
                            map.put(k.toString(), v);
                        });
                        ConfigurableEnvironment environment = new StandardEnvironment();
                        MutablePropertySources propertySources = environment.getPropertySources();
                        propertySources.addFirst(new MapPropertySource(listOfFile.getName(), map));

                        setUpApplicationContext(environment, properties);

                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String stackTrace = sw.toString();
                        LOG.error("Exception: " + e.getMessage());
                        LOG.error("StackTrace: " + stackTrace);
                    }
                }
            }
        }
    }

    public static void setUpApplicationContext(ConfigurableEnvironment environment, Properties properties) {
        @SuppressWarnings("resource")
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.registerShutdownHook();
        ctx.setEnvironment(environment);

        // TODO: need a proper way to validate input properties specified by user
        String executionMode = environment.getProperty("execution.mode", "local").toLowerCase();
        if (executionMode != "local" && executionMode != "remote")
            throw new RuntimeException("Invalid execution mode specified. Must be `local` (default) or `remote`.");

        String instanceType = "";
        if (executionMode == "remote")
        {
            if (!environment.containsProperty("execution.instanceType")) {
                throw new RuntimeException("Instance type in remote execution not specified. Must be `master` or `slave`.");
            }

            instanceType = environment.getRequiredProperty("execution.instanceType").toLowerCase();
            if (instanceType != "master" && instanceType != "slave")
                throw new RuntimeException("Invalid instance type in remote execution mode specified. Must be `master` or `slave`.");
        }

        boolean useScheduling;
        try {
            useScheduling = Boolean.parseBoolean(environment.getProperty("scheduler.useScheduling", "false"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid scheduling option specified. Must be `true` or `false` (default).");
        }

        // set appropriate job configuration
        if (executionMode == "remote" && instanceType == "slave") {
            ctx.register(JobConfiguration.class);
            ctx.refresh();
        } else { // execution mode local or remote with master
            if (useScheduling) {
                ctx.register(ScheduledJobLauncher.class);
                ctx.refresh();
            } else {
                ctx.register(SingleJobLauncher.class);
                ctx.refresh();
                SingleJobLauncher launcher = ctx.getBean(SingleJobLauncher.class);
                launcher.launchJob();
            }
        }
    }
}
