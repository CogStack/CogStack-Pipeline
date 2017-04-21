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
package uk.ac.kcl.listeners;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.ac.kcl.partitioners.CogstackJobPartitioner;
import uk.ac.kcl.service.ESRestService;

@Component
//@Scope("prototype")
public class JobCompleteNotificationListener implements JobExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(JobCompleteNotificationListener.class);
  private long timeOfNextJob;

  public void setLastDateInthisJob(long l){
    this.timeOfNextJob = l;
  }

  @Autowired
  CogstackJobPartitioner columnRangePartitioner;

  @Autowired(required = false)
  @Qualifier("esRestService")
  private ESRestService esRestService;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    columnRangePartitioner.setJobExecution(jobExecution);
  }

  @Autowired
  JobRepository jobRepository;

  @Override
  public synchronized void afterJob(JobExecution jobExecution) {
    if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
      if(columnRangePartitioner.isFirstRun()){
        columnRangePartitioner.setFirstRun(false);
      }
      log.info("!!! JOB FINISHED! promoting last good record date to JobExecutionContext");
      jobExecution.getExecutionContext().put("last_successful_timestamp_from_this_job", timeOfNextJob);
      jobRepository.updateExecutionContext(jobExecution);
    }

    if (esRestService != null) {
      // Workaround to close ElasticSearch REST properly (the job was stuck before this change)
      try {
        esRestService.destroy();
      } catch (IOException e) {
        log.warn("IOException when destroying ElasticSearch REST service");
      }
    }
  }


}
