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
import java.io.File;

@Component
public class DocumentRowMapper implements RowMapper<Document>{
    private static final Logger LOG = LoggerFactory.getLogger(DocumentRowMapper.class);
    @Autowired
    Environment env;

    @Autowired
    BatchJobUtils batchJobUtils;
    @Autowired
    ApplicationContext context;

    // mandatory properties required to perform record mapping
    @Value("${source.primaryKeyFieldValue}")
    private String primaryKeyFieldValue;
    @Value("${source.timeStamp}")
    private String timeStamp;

    // optional fields, required in docman profile
    @Value("${source.srcTableName:#{null}}")
    private String srcTableName;
    @Value("${source.srcColumnFieldName:#{null}}")
    private String srcColumnFieldName;
    @Value("${source.primaryKeyFieldName:#{null}}")
    private String primaryKeyFieldName;

    // profile-specific properties used when performing mapping
    @Value("${reindexColumn:#{null}}")
    private String reindexColumn;
    @Value("${reindex:false}")
    private boolean reindex;
    @Value("${reindexField:#{null}}")
    private String reindexField;

    @Value("${elasticsearch.datePattern:yyyy-MM-dd'T'HH:mm:ss.SSS}")
    private String esDatePattern;

    @Value("${tika.binaryContentSource:database}")
    private String binaryContentSource;
    @Value("${tika.binaryPathPrefix:#{null}}")
    private String pathPrefix;
    @Value("${tika.binaryFileExts:#{null}}")
    private String fileExts;
    @Value("${tika.binaryFieldName:#{null}}")
    private String binaryContentFieldName;


    private DateTimeFormatter eSCompatibleDateTimeFormatter;
    private List<String> fieldsToIgnore;
    
    private String[] possibleFileExtensions;


    @PostConstruct
    public void init () {
        fieldsToIgnore = Arrays.asList(env.getProperty("elasticsearch.excludeFromIndexing", "")
                .toLowerCase().split(","));
        eSCompatibleDateTimeFormatter = DateTimeFormat.forPattern(esDatePattern);
        if (null != fileExts){
            possibleFileExtensions = fileExts.split(",");
            LOG.info("possible file extensions: " + fileExts);
        }        
    }

    private void mapDBFields(Document doc, ResultSet rs) throws SQLException, IOException {
        //add additional query fields for ES export
        ResultSetMetaData meta = rs.getMetaData();

        int colCount = meta.getColumnCount();

        for (int col=1; col <= colCount; col++){
            Object value = rs.getObject(col);
            if (value != null){
                String colLabel =meta.getColumnLabel(col).toLowerCase();
                if(!fieldsToIgnore.contains(colLabel)){
                    DateTime dateTime;
                    //map correct SQL time types
                    switch (meta.getColumnType(col)){
                        case 91:
                            Date dt = (Date)value;
                            dateTime = new DateTime(dt.getTime());
                            doc.getAssociativeArray().put(meta.getColumnLabel(col).toLowerCase(), eSCompatibleDateTimeFormatter.print(dateTime));
                            break;
                        case 93:
                            Timestamp ts = (Timestamp) value;
                            dateTime = new DateTime(ts.getTime());
                            doc.getAssociativeArray().put(meta.getColumnLabel(col).toLowerCase(), eSCompatibleDateTimeFormatter.print(dateTime));
                            break;
                        default:
                            doc.getAssociativeArray().put(meta.getColumnLabel(col).toLowerCase(), rs.getString(col));
                            break;
                    }
                }
            }

            //map binary content from FS or database if required (as per docman reader)
            if(value != null && meta.getColumnLabel(col).equalsIgnoreCase(binaryContentFieldName)) {
                switch (binaryContentSource) {
                    case "database":
                        doc.setBinaryContent(rs.getBytes(col));
                        break;
                    case "fileSystemWithDBPath":
                        for (int i=0;possibleFileExtensions!=null && i<possibleFileExtensions.length;i++){
                            String fileFullPath = pathPrefix + rs.getString(col) + possibleFileExtensions[i];
                            File f = new File(fileFullPath);
                            if(f.exists() && !f.isDirectory()) {
                                LOG.info("File found, working on " + fileFullPath);
                                Resource resource = context.getResource("file:" + fileFullPath);
                                doc.setBinaryContent(IOUtils.toByteArray(resource.getInputStream()));
                                break;
                            }                           
                        }                        
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void mapDBMetadata(Document doc, ResultSet rs) throws SQLException {
        if (Arrays.asList(this.env.getActiveProfiles()).contains("docman")) {
            // these fields are only used when reading documents from metadata tables using docman profile
            doc.setSrcTableName(rs.getString(srcTableName));
            doc.setSrcColumnFieldName(rs.getString(srcColumnFieldName));
            doc.setPrimaryKeyFieldName(rs.getString(primaryKeyFieldName));
        }
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
                mapDBMetadata(doc, rs);
                mapDBFields(doc, rs);
            } catch (IOException e) {
                LOG.error("DocumentRowMapper could not map file based binary, nested exception: {}", e);
                throw new SQLException("DocumentRowMapper could not map file based binary");
            }
        }
        return doc;
    }

    private void mapAssociativeArray(Document doc, ResultSet rs) throws SQLException {
        mapDBMetadata(doc, rs);
        doc.setAssociativeArray(new Gson().fromJson(rs.getString(reindexField),
                new TypeToken<HashMap<String, Object>>() {}.getType()));
    }
}
