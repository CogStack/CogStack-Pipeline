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
package uk.ac.kcl.scheduling;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.kcl.batch.JobConfiguration;

/**
 *
 * @author King's College London, Richard Jackson <richgjackson@gmail.com>
 */
@Service
@Import(JobConfiguration.class)
@EnableScheduling
public class ScheduledJobLauncher extends SingleJobLauncher {




    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ScheduledJobLauncher.class);

    @Scheduled(cron = "${scheduler.rate}")
    public void doTask()  {
        launchJob();
    }
}
