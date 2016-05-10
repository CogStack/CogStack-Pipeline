package uk.ac.kcl.model

import java.sql.Date
import java.sql.Timestamp

/**
 * Created by rich on 15/04/16.
 */
class Document {
    String databaseName
    String databaseSchema
    String srcTableName
    String srcColumnFieldName
    String primaryKeyFieldName
    String primaryKeyFieldValue
    Timestamp timeStamp
    String outputData
    LinkedHashMap<String,Object> additionalFields = new HashMap<String,Object>();
}
