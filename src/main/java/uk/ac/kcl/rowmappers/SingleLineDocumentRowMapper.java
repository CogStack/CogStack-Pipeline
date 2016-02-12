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
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author rich
 */
public class SingleLineDocumentRowMapper implements RowMapper<SimpleDocument>{

    String documentKeyName;
    String lineKeyName;
    String lineContents;
    @Override
    public SimpleDocument mapRow(ResultSet rs, int i) throws SQLException {
        SimpleDocument doc = new SimpleDocument();
        doc.setDocumentKey(rs.getString(documentKeyName));
        doc.setLineKey(rs.getString(lineKeyName));        
        doc.setLineContents(rs.getString(lineContents));
        return doc;
    }
    
}
