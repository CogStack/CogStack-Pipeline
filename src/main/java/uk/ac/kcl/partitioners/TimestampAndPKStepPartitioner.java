package uk.ac.kcl.partitioners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * Created by rich on 09/06/16.
 */
@Service
//@Profile("primaryKeyAndTimeStampPartition")
public class TimestampAndPKStepPartitioner implements StepPartitioner {
    @Autowired
    Environment env;
    @Value("${partitioner.timeStampColumnName:#{null}}")
    String timeStamp;

    @Value("${source.dbmsToJavaSqlTimestampType}")
    String dbmsToJavaSqlTimestampType ;

    @Value("${partitioner.pkColumnNameToPartition}")
    String column;


    private static final Logger LOG = LoggerFactory.getLogger(TimestampAndPKStepPartitioner.class);

    @Override
    public String getPartitioningLogic(String minValue, String maxValue, String minTimeStamp, String maxTimeStamp){
        String returnString = null;
        if( minTimeStamp!= null && maxTimeStamp != null) {
            returnString = MessageFormat.format("WHERE {0}  BETWEEN CAST(''{1}'' AS {2} ) AND CAST(''{3}'' AS {2}) "
                    + " AND {4} BETWEEN ''{5}'' AND ''{6}''",timeStamp,minTimeStamp,dbmsToJavaSqlTimestampType
                    ,maxTimeStamp,column,minValue,maxValue);
        }
        LOG.info("This step where clause: " + returnString);
        return returnString;
    }
}
