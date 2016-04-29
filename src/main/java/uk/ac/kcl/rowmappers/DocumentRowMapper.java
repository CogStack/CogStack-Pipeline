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
import org.springframework.stereotype.Component;
import uk.ac.kcl.model.BinaryDocument;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.kcl.model.Document;

@Component
public class DocumentRowMapper implements RowMapper<Document>{

    @Autowired
    Environment env;

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
        doc.setSrcTableName(rs.getString(env.getProperty("srcTableName")));
        doc.setSrcColumnFieldName(rs.getString(env.getProperty("srcColumnFieldName")));
        doc.setPrimaryKeyFieldName(rs.getString(env.getProperty("primaryKeyFieldName")));
        doc.setPrimaryKeyFieldValue(rs.getString(env.getProperty("primaryKeyFieldValue")));
        doc.setTimeStamp(rs.getDate(env.getProperty("timeStamp")));

        //add additional query fields for ES export
        ResultSetMetaData meta = rs.getMetaData();
        List<String> fieldsToIgnore = Arrays.asList(env.getProperty("elasticsearch.excludeFromIndexing").split(","));
        int colCount = meta.getColumnCount();

            for (int col=1; col <= colCount; col++)
            {
                Object value = rs.getObject(col);
                if (value != null && !fieldsToIgnore.contains(meta.getColumnLabel(col)))
                {
                    if(meta.getColumnType(col)==91) {
                        Date dt = (Date)value;
                        Timestamp ts = new Timestamp(dt.getTime());
                        doc.getAdditionalFields().put(meta.getColumnLabel(col), ts.toString());
                    }else if (meta.getColumnType(col)==93){
                        Timestamp ts = (Timestamp) value;
                        doc.getAdditionalFields().put(meta.getColumnLabel(col), ts.toString());
                    }else {
                        doc.getAdditionalFields().put(meta.getColumnLabel(col), value);
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
