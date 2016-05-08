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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.utils.BatchJobUtils;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

@Component
public class DocumentRowMapper implements RowMapper<Document>{

    @Autowired
    Environment env;

    @Autowired
    BatchJobUtils batchJobUtils;


    @PostConstruct
    public void init (){
        srcTableName = env.getProperty("srcTableName");
        srcColumnFieldName = env.getProperty("srcColumnFieldName");
        primaryKeyFieldName =env.getProperty("primaryKeyFieldName");
        primaryKeyFieldValue = env.getProperty("primaryKeyFieldValue");
        timeStamp = env.getProperty("timeStamp");
        fieldsToIgnore = Arrays.asList(env.getProperty("elasticsearch.excludeFromIndexing").split(","));
    }

    private String srcTableName;
    private String srcColumnFieldName;
    private String primaryKeyFieldName;
    private String primaryKeyFieldValue;
    private String timeStamp;
    private List<String> fieldsToIgnore;



    protected void mapFields(Document doc, ResultSet rs) throws SQLException {
//implement later if database name/schema is requried
//        if(rs.getString(env.getProperty("databaseName"))==null){
//            doc.setDatabaseName(null);
//        }else{
//            doc.setDatabaseName(rs.getString(env.getProperty("databaseName")));
//        }
//        if(rs.getString(env.getProperty("databaseSchema"))==null){
//            doc.setDatabaseSchema(null);
//        }else{
//            doc.setDatabaseSchema(rs.getString(env.getProperty("databaseName")));
//        }
        doc.setSrcTableName(rs.getString(srcTableName));
        doc.setSrcColumnFieldName(rs.getString(srcColumnFieldName));
        doc.setPrimaryKeyFieldName(rs.getString(primaryKeyFieldName));
        doc.setPrimaryKeyFieldValue(rs.getString(primaryKeyFieldValue));
        doc.setTimeStamp(rs.getDate(timeStamp));

        //add additional query fields for ES export
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

            for (int col=1; col <= colCount; col++)
            {
                Object value = rs.getObject(col);
                if (value != null && !fieldsToIgnore.contains(meta.getColumnLabel(col)))
                {
                    if(meta.getColumnType(col)==91) {
                        Date dt = (Date)value;
                        Timestamp ts = new Timestamp(dt.getTime());
                        doc.getAdditionalFields().put(meta.getColumnLabel(col), batchJobUtils.convertTimeStampToESDateFormat(ts));
                    }else if (meta.getColumnType(col)==93){
                        Timestamp ts = (Timestamp) value;
                        doc.getAdditionalFields().put(meta.getColumnLabel(col), batchJobUtils.convertTimeStampToESDateFormat(ts));
                    }else {
                        doc.getAdditionalFields().put(meta.getColumnLabel(col), rs.getString(col));
                    }

                }
            }


    }

    @Override
    public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
        Document doc = new Document();
        mapFields(doc,rs);
        return doc;
    }
}
