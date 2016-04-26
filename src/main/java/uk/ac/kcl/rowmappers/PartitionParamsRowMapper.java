package uk.ac.kcl.rowmappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.PartitionParams;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by rich on 26/04/16.
 */
@Service("partitionParamsRowMapper")
public class PartitionParamsRowMapper implements RowMapper<PartitionParams>{

    @Autowired
    Environment env;
    @Override
    public PartitionParams mapRow(ResultSet rs, int rowNum) throws SQLException {
        PartitionParams params = new PartitionParams();
        params.setMaxTimeStamp(rs.getTimestamp("max_time_stamp"));
        params.setMinTimeStamp(rs.getTimestamp("min_time_stamp"));
        params.setMaxId(rs.getLong("max_id"));
        params.setMinId(rs.getLong("min_id"));
        return params;

    }
}