package uk.ac.kcl.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder

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
    HashSet<RuntimeException> exceptions = new HashSet<>()
    Gson gson = new GsonBuilder().create();

    //for catpuring itemProcessor output
    //XContentBuilder xContentBuilder

    String outputData;

    //for tika
    byte[] binaryContent

    //for Gate
    String textContent


    //for es
    HashMap<String,Object> associativeArray = new HashMap<String,Object>();

    public String getDocName(){
        return srcTableName+"_"+srcColumnFieldName+"_"+primaryKeyFieldValue
    }

}
