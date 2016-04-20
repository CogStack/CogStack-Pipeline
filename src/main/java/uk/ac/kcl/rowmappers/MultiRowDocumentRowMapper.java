package uk.ac.kcl.rowmappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.MultilineDocument;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service("multiRowDocumentRowmapper")
public class MultiRowDocumentRowMapper implements RowMapper<MultilineDocument> {
    public MultiRowDocumentRowMapper(){}
    @Autowired
    @Qualifier("sourceDataSource")
    private DataSource ds;
    @Autowired
    public SimpleDocumentRowMapper simpleMapper;

    private JdbcTemplate template;

    @Resource
    Environment env;



    @PostConstruct
    public void init(){
        this.template = new JdbcTemplate(ds);
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

        List<MultilineDocument> docs = template.query(sql.toString(), simpleMapper);

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