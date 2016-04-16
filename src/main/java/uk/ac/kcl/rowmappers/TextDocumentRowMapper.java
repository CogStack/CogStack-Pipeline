package uk.ac.kcl.rowmappers;

import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.TextDocument;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by rich on 16/04/16.
 */
public class TextDocumentRowMapper extends DocumentRowMapper {

    @Override
    public Document mapRow(ResultSet rs, int i) throws SQLException {
        Document doc = new TextDocument(rs.getString(env.getProperty("textFieldName")));
        mapFields(doc,rs);
        return doc;
    }

}
