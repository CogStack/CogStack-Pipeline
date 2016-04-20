package uk.ac.kcl.rowmappers;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.model.Document;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by rich on 16/04/16.
 */
@Service("binaryDocumentRowMapper")
public class BinaryDocumentRowMapper<B extends Document> extends DocumentRowMapper {

    public BinaryDocumentRowMapper(){};
    @Override
    public Document mapRow(ResultSet rs, int i) throws SQLException {
        //ResultSetMetaData meta = rs.getMetaData();
        Document doc = new BinaryDocument(rs.getBytes(env.getProperty("binaryFieldName")));
        mapFields(doc,rs);
        return doc;
    }

}
