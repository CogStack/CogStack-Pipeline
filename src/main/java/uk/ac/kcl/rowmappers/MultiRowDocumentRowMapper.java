/* 
 * Copyright 2016 King's College London, Richard Jackson <richgjackson@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.kcl.rowmappers;

import uk.ac.kcl.model.SimpleDocument;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author rich
 */
public class MultiRowDocumentRowMapper implements RowMapper<SimpleDocument> {

    private final DataSource ds;
    private String documentKeyName;
    private String lineKeyName;
    private String lineContents;
    private String tableName;

    public MultiRowDocumentRowMapper(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public SimpleDocument mapRow(ResultSet rs, int i) throws SQLException {

        JdbcTemplate template = new JdbcTemplate(ds);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(documentKeyName)
                .append(", ")
                .append(lineKeyName)
                .append(", ")
                .append(lineContents)
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(documentKeyName)
                .append(" = ")
                .append(rs.getString(documentKeyName))
                .append(" ORDER BY ")
                .append(lineKeyName)
                .append(" DESC");
        SingleLineDocumentRowMapper mapper = new SingleLineDocumentRowMapper();
        mapper.setDocumentKeyName(documentKeyName);
        mapper.setLineContents(lineContents);
        mapper.setLineKeyName(lineKeyName);
        List<SimpleDocument> docs = template.query(sql.toString(),
                mapper);

        TreeMap<Integer, String> map = new TreeMap<>();
        for (SimpleDocument doc : docs) {
            map.put(Integer.valueOf(doc.getLineKey()), doc.getLineContents());
        }

        SimpleDocument doc = new SimpleDocument();
        doc.setDocumentKey(rs.getString(documentKeyName));

        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            sb2.append(entry.getValue());

        }
        doc.setLineContents(sb2.toString());

        return doc;

    }

    public String getDocumentKeyName() {
        return documentKeyName;
    }

    public void setDocumentKeyName(String documentKeyName) {
        this.documentKeyName = documentKeyName;
    }

    public String getLineKeyName() {
        return lineKeyName;
    }

    public void setLineKeyName(String lineKeyName) {
        this.lineKeyName = lineKeyName;
    }

    public String getLineContents() {
        return lineContents;
    }

    public void setLineContents(String lineContents) {
        this.lineContents = lineContents;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
