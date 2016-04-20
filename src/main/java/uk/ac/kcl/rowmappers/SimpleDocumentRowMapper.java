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
import uk.ac.kcl.model.MultilineDocument;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.PostConstruct;

/**
 *
 * @author rich
 */
@Component
public class SimpleDocumentRowMapper implements RowMapper<MultilineDocument>{

    public SimpleDocumentRowMapper(){};
    private String documentKeyName;
    private String lineKeyName;
    private String lineContents;
    @Autowired
    Environment env;



    @Override
    public MultilineDocument mapRow(ResultSet rs, int i) throws SQLException {
        MultilineDocument doc = new MultilineDocument();
        doc.setDocumentKey(rs.getString(env.getProperty("lf.documentKeyName")));
        doc.setLineKey(rs.getString(env.getProperty("lf.lineKeyName")));
        doc.setLineContents(rs.getString(env.getProperty("lf.lineContents")));
        return doc;
    }

    public String getDocumentKeyName() {
        return documentKeyName;
    }
    
}
