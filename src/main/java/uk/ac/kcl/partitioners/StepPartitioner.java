package uk.ac.kcl.partitioners;

/**
 * Created by rich on 09/06/16.
 */
public interface StepPartitioner {
     String getPartitioningLogic(String minValue, String maxValue, String minTimeStamp, String maxTimeStamp);
}
