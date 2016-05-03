package uk.ac.kcl.model
import java.sql.Timestamp

/**
 * Created by rich on 26/04/16.
 */
class ScheduledPartitionParams  extends PartitionParams{
    Timestamp minTimeStamp
    Timestamp maxTimeStamp
}