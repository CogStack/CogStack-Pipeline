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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.pdfbox.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Component
public class DocumentRowMapper implements RowMapper<Document>{
    private static final Logger LOG = LoggerFactory.getLogger(DocumentRowMapper.class);
    @Autowired
    Environment env;

    @Autowired
    BatchJobUtils batchJobUtils;
    @Autowired
    ApplicationContext context;
    @Value("${reindexColumn:#{null}}")
    private String reindexColumn;
    @Value("${datePatternForES}")
    private String esDatePattern;
    @Value("${reindex:false}")
    private boolean reindex;
    @Value("${reindexField:#{null}}")
    private String reindexField;
    @Value("${tika.binaryPathPrefix:#{null}}")
    private String pathPrefix;
    @Value("${tika.binaryContentSource:#{null}}")
    private String binaryContentSource;
    @Value("${tika.binaryFieldName:#{null}}")
    private String binaryContentFieldName;
    @Value("${srcTableName}")
    private String srcTableName;
    @Value("${srcColumnFieldName}")
    private String srcColumnFieldName;
    @Value("${primaryKeyFieldName}")
    private String primaryKeyFieldName;
    @Value("${primaryKeyFieldValue}")
    private String primaryKeyFieldValue;
    @Value("${timeStamp}")
    private String timeStamp;
    private DateTimeFormatter fmt;
    private List<String> fieldsToIgnore;

    @PostConstruct
    public void init () {
        fieldsToIgnore = Arrays.asList(env.getProperty("elasticsearch.excludeFromIndexing")
                .toLowerCase().split(","));
        fmt = DateTimeFormat.forPattern(esDatePattern);
    }
    void mapFields(Document doc, ResultSet rs) throws SQLException, IOException {
        mapMetadata(doc, rs);
        //add additional query fields for ES export
        ResultSetMetaData meta = rs.getMetaData();

        int colCount = meta.getColumnCount();

        for (int col=1; col <= colCount; col++){
            Object value = rs.getObject(col);
            if (value != null){
                String colLabel =meta.getColumnLabel(col).toLowerCase();
                if(!fieldsToIgnore.contains(colLabel)){
                    if(meta.getColumnType(col)==91) {
                        Date dt = (Date)value;
                        DateTime dateTime = new DateTime(dt.getTime());
                        doc.getAssociativeArray().put(meta.getColumnLabel(col).toLowerCase(), fmt.print(dateTime));
                    }else if (meta.getColumnType(col)==93){
                        Timestamp ts = (Timestamp) value;
                        DateTime dateTime = new DateTime(ts.getTime());
                        doc.getAssociativeArray().put(meta.getColumnLabel(col).toLowerCase(), fmt.print(dateTime));
                    }else {
                        doc.getAssociativeArray().put(meta.getColumnLabel(col).toLowerCase(), rs.getString(col));
                    }
                }
            }
            if (binaryContentSource!= null) {
                if (binaryContentSource.equals("database")) {
                    if (binaryContentFieldName != null &&
                            value != null
                            && meta.getColumnLabel(col).equalsIgnoreCase(binaryContentFieldName)) {
                        doc.setBinaryContent(rs.getBytes(col));
                    }
                } else if (binaryContentSource.equals("fileSystemWithDBPath") &&
                        value != null
                        && meta.getColumnLabel(col).equalsIgnoreCase(binaryContentFieldName)) {
                    Resource resource = context.getResource(pathPrefix + rs.getString(col));
                    doc.setBinaryContent(IOUtils.toByteArray(resource.getInputStream()));
                }
            }
        }
    }

    private void mapMetadata(Document doc, ResultSet rs) throws SQLException {
        doc.setSrcTableName(rs.getString(srcTableName));
        doc.setSrcColumnFieldName(rs.getString(srcColumnFieldName));
        doc.setPrimaryKeyFieldName(rs.getString(primaryKeyFieldName));
        doc.setPrimaryKeyFieldValue(rs.getString(primaryKeyFieldValue));
        doc.setTimeStamp(rs.getTimestamp(timeStamp));
    }

    @Override
    public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
        Document doc = new Document();
        if(reindex){
            mapAssociativeArray(doc, rs);
        }else {
            try {
                mapFields(doc, rs);
            } catch (IOException e) {
                LOG.error("DocumentRowMapper could not map file based binary");
                throw new SQLException("DocumentRowMapper could not map file based binary");
            }
        }
        return doc;
    }

    private void mapAssociativeArray(Document doc, ResultSet rs) throws SQLException {
        mapMetadata(doc, rs);
        doc.setAssociativeArray(new Gson().fromJson(rs.getString(reindexField),
                new TypeToken<HashMap<String, Object>>() {}.getType()));
    }
}
