package uk.ac.kcl.rowmappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.kcl.model.MultilineDocument;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MultiRowDocumentRowMapper implements RowMapper<MultilineDocument> {
    private final DataSource ds;
    private final SimpleDocumentRowMapper mapper;
    private final JdbcTemplate template;


    @Autowired
    Environment env;

    public MultiRowDocumentRowMapper(DataSource ds){
        this.ds = ds;
        this.mapper = new SimpleDocumentRowMapper();
        this.template = new JdbcTemplate(ds);

        mapper.setDocumentKeyName(env.getProperty("documentKeyName"));
        mapper.setLineContents(env.getProperty("lineContents"));
        mapper.setLineKeyName(env.getProperty("lineKeyName"));
    }

    @Override
    public MultilineDocument mapRow(ResultSet rs, int i) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(env.getProperty("lf.documentKeyName"))
                .append(", ")
                .append(env.getProperty("lf.lineKeyName"))
                .append(", ")
                .append(env.getProperty("lf.lineContents"))
                .append(" FROM ")
                .append(env.getProperty("lf.srcTableName"))
                .append(" WHERE ")
                .append(env.getProperty("lf.documentKeyName"))
                .append(" = ")
                .append(rs.getLong(env.getProperty("lf.documentKeyName")))
                .append(" ORDER BY ")
                .append(env.getProperty("lf.lineKeyName"))
                .append(" DESC");

        List<MultilineDocument> docs = template.query(sql.toString(), mapper);

        TreeMap<Integer, String> map = new TreeMap<>();
        for (MultilineDocument doc : docs) {
            map.put(Integer.valueOf(doc.getLineKey()), doc.getLineContents());
        }
        MultilineDocument doc = new MultilineDocument();
        doc.setDocumentKey(rs.getString(env.getProperty("lf.documentKeyName")));
        doc.setTimeStamp(rs.getString(env.getProperty("lf.timeStamp")));

        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            sb2.append(entry.getValue());
        }
        doc.setLineContents(sb2.toString());
        return doc;
    }
}