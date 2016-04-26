package uk.ac.kcl.model

import java.sql.Date

/**
 * Created by rich on 15/04/16.
 */
abstract class Document {
    String databaseName
    String databaseSchema
    String srcTableName
    String srcColumnFieldName
    String primaryKeyFieldName
    String primaryKeyFieldValue
    Date timeStamp
    String outputData
    LinkedHashMap<String,Object> additionalFields = new HashMap<String,Object>();
}
