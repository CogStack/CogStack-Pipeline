package uk.ac.kcl.partitioners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by rich on 09/06/16.
 */
@Service
@Profile("primaryKeyPartition")
public class PKStepPartitioner implements StepPartitioner {
    @Autowired
    Environment env;

    private static final Logger LOG = LoggerFactory.getLogger(PKStepPartitioner.class);

    @Override
     public String getPartitioningLogic(String minValue, String maxValue, String minTimeStamp, String maxTimeStamp){
        String
            returnString = ("WHERE " + env.getProperty("columnToProcess")
                    + " BETWEEN " + minValue + " AND " + maxValue) ;

        LOG.info("This step where clause: " + returnString);
        return returnString;
    }
}
