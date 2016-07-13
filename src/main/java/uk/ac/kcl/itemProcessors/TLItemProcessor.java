package uk.ac.kcl.itemProcessors;

import uk.ac.kcl.model.Document;

/**
 * Created by rich on 25/05/16.
 */
public abstract class TLItemProcessor {
    private String fieldName;

    void setFieldName(String fieldName){
        this.fieldName = fieldName;
    }

    void addField(Document doc, Object content){
        doc.getAdditionalFields().put(fieldName, content);

    }

}
