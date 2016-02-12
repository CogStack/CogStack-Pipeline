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

import uk.ac.kcl.model.BinaryDocument;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author rich
 * @param <T>
 */
public class DocumentMetadataRowMapper<T> implements RowMapper<BinaryDocument>{

    private List<String> otherFieldsList;
    
    
    
    @Override
    public BinaryDocument mapRow(ResultSet rs, int i) throws SQLException {
        //ResultSetMetaData meta = rs.getMetaData();
        BinaryDocument doc = new BinaryDocument();
        
        for(String entry : otherFieldsList){
            doc.getMetadata().put(entry,rs.getString(entry));
        }

        return doc;
    }

    public void setOtherFieldsList(List<String> otherFieldsList) {
        this.otherFieldsList = otherFieldsList;
    }
    
}
