package uk.ac.kcl.rowmappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
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
public class MultiRowDocumentRowMapper <B extends Document> extends DocumentRowMapper {
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
    public Document mapRow(ResultSet rs, int i) throws SQLException {

        String sql = "SELECT " +
                env.getProperty("lf.documentKeyName") +
                ", " +
                env.getProperty("lf.lineKeyName") +
                ", " +
                env.getProperty("lf.lineContents") +
                " FROM " +
                env.getProperty("lf.srcTableName") +
                " WHERE " +
                env.getProperty("lf.documentKeyName") +
                " = " +
                rs.getLong(env.getProperty("lf.documentKeyName")) +
                " ORDER BY " +
                env.getProperty("lf.lineKeyName") +
                " DESC";

        List<MultilineDocument> docs = template.query(sql, simpleMapper);

        TreeMap<Integer, String> map = new TreeMap<>();
        for (MultilineDocument doc : docs) {
            map.put(Integer.valueOf(doc.getLineKey()), doc.getLineContents());
        }
        Document doc = new Document();
        mapFields(doc,rs);

        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            sb2.append(entry.getValue());
        }
        doc.setOutputData(sb2.toString());
        return doc;
    }
}