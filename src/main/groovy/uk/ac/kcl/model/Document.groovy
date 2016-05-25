package uk.ac.kcl.model

import java.sql.Date
import java.sql.Timestamp

/**
 * Created by rich on 15/04/16.
 */
class Document {

    //generic fields
    String databaseName
    String databaseSchema
    String srcTableName
    String srcColumnFieldName
    String primaryKeyFieldName
    String primaryKeyFieldValue
    Timestamp timeStamp

    //for catpuring itemProcessor output
    String outputData

    //for tika
    byte[] binaryContent

    //for Gate
    String textContent

    //for es
    LinkedHashMap<String,Object> additionalFields = new HashMap<String,Object>();

    public String getDocName(){
        return srcTableName+"_"+srcColumnFieldName+"_"+primaryKeyFieldValue
    }
}
