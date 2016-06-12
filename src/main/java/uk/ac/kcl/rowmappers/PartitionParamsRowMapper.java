package uk.ac.kcl.rowmappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.PartitionParams;
import uk.ac.kcl.model.ScheduledPartitionParams;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by rich on 26/04/16.
 */
@Service("partitionParamsRowMapper")
public class PartitionParamsRowMapper implements RowMapper<PartitionParams>{
    @Override
    public PartitionParams mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();

        int numberOfColumns = meta.getColumnCount();
        ArrayList<String> columnNames = new ArrayList<>();
// get the column names; column indexes start from 1
        for (int i = 1; i < numberOfColumns + 1; i++) {
            columnNames.add(meta.getColumnName(i));
        }


        if(!columnNames.contains("max_time_stamp")) {
            PartitionParams params = new PartitionParams();
            params.setMaxId(rs.getLong("max_id"));
            params.setMinId(rs.getLong("min_id"));
            return params;
        }else{
            ScheduledPartitionParams params = new ScheduledPartitionParams();
            params.setMaxId(rs.getLong("max_id"));
            params.setMinId(rs.getLong("min_id"));
            params.setMaxTimeStamp(rs.getTimestamp("max_time_stamp"));
            params.setMinTimeStamp(rs.getTimestamp("min_time_stamp"));
            return params;
        }
    }
}