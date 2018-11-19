package uk.ac.kcl.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.sql.Timestamp

/**
 * Created by rich on 15/04/16.
 */
class Document {

    //generic fields
    //String databaseName
    //String databaseSchema

    // used when processing documents from metadata tables within docman profile
    String srcTableName
    String srcColumnFieldName
    String primaryKeyFieldName

    // universal fields
    String primaryKeyFieldValue
    Timestamp timeStamp

    // auxiliary members
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

    String getDocName(){
        String name = ""
        if (srcTableName != null && srcTableName.length() > 0)
            name += srcTableName + "_"
        if (srcColumnFieldName != null && srcColumnFieldName.length() > 0)
            name += srcColumnFieldName + "_"
        return name + primaryKeyFieldValue
    }

}
