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
package uk.ac.kcl.model;

import java.util.HashMap;

/**
 *
 * @author rich
 */
public class BinaryDocument {
    private final HashMap<String, String> metadata;
    private final byte[] body;
    
    
    public String getXhtml(){
        return metadata.get("xhtml");
    }    
    
    public String getId(){
        return metadata.get("id");
    }    
    
    public String getSrcColumnFieldName(){
        return metadata.get("srcColumnFieldName");
    }      
    
    public String getSrcTableName(){
        return metadata.get("srcTableName");
    }      
    
    public String getPrimaryKeyFieldName(){
        return metadata.get("primaryKeyFieldName");
    }        
    
    public String getPrimaryKeyFieldValue(){
        return metadata.get("primaryKeyFieldValue");
    }     
    
    public String getBinaryFieldName(){
        return metadata.get("binaryFieldName");
    }      
    
    public String getUpdateTime(){
        return metadata.get("updateTime");
    }        
    
    public String getGateData(){
        return metadata.get("gateData");
    }   
            
    public BinaryDocument(byte[] body) {
        this.metadata = new HashMap<>();
        this.body = body;
    }
    public BinaryDocument(String body) {
        this.metadata = new HashMap<>();
        this.body = body.getBytes();
    }    

    public BinaryDocument() {
        this.metadata = new HashMap<>();
        this.body = null;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public byte[] getBody() {
        return body;
    }
    
}
