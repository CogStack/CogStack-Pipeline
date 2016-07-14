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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.partitioners.AbstractRealTimeRangePartitioner;

import java.util.Iterator;
import java.util.Set;

@Service
@Qualifier("nonFatalExceptionItemProcessorListener")
public class NonFatalExceptionItemProcessorListener implements ItemProcessListener<Document,Document> {


	@Override
	public void beforeProcess(Document item) {


	}

	@Override
	public void afterProcess(Document item, Document result) {


	}

	@Override
	public void onProcessError(Document item, Exception e) {

	}
}