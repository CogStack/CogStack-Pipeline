package uk.ac.kcl.model

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
    String timeStamp
    String outputData
}
