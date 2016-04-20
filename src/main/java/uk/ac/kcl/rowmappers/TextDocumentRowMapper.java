package uk.ac.kcl.rowmappers;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.TextDocument;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by rich on 16/04/16.
 */
@Service("textDocumentRowMapper")
public class TextDocumentRowMapper<T extends Document> extends DocumentRowMapper {

    public TextDocumentRowMapper(){};
    @Override
    public Document mapRow(ResultSet rs, int i) throws SQLException {
        Document doc = new TextDocument(rs.getString(env.getProperty("textFieldName")));
        mapFields(doc,rs);
        return doc;
    }

}
