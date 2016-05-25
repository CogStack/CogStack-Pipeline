package uk.ac.kcl.itemProcessors;

import uk.ac.kcl.model.Document;

/**
 * Created by rich on 25/05/16.
 */
public abstract class TLItemProcessor {
    private String fieldName;

    public void setFieldName(String fieldName){
        this.fieldName = fieldName;
    }

    public void addField(Document doc, String content){
        doc.getAdditionalFields().put(fieldName, content);

    }

}
