package uk.ac.kcl.mutators;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rich on 05/07/16.
 */
public class Mutant {
    private List<String> inputTokens;
    private List<String> outputTokens;
    private String finalText;

    public String getDeidentifiedString() {
        return deidentifiedString;
    }

    public void setDeidentifiedString(String deidentifiedString) {
        this.deidentifiedString = deidentifiedString;
    }

    private String deidentifiedString;


    public long getDocumentid() {
        return documentid;
    }

    public void setDocumentid(long documentid) {
        this.documentid = documentid;
    }

    private long documentid;
    public Mutant(){
        this.inputTokens=new ArrayList<>();
        this.outputTokens=new ArrayList<>();
    }

    public List<String> getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(List<String> inputTokens) {
        this.inputTokens = inputTokens;
    }

    public List<String> getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(List<String> outputTokens) {
        this.outputTokens = outputTokens;
    }

    public String getFinalText() {
        return finalText;
    }

    public void setFinalText(String finalText) {
        this.finalText = finalText;
    }
}
