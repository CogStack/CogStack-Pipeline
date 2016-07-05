package uk.ac.kcl.mutators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rich on 05/07/16.
 */
public class Mutant {
    private Set<String> inputTokens;
    private Set<String> outputTokens;
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
        this.inputTokens=new HashSet<>();
        this.outputTokens=new HashSet<>();
    }

    public Set<String> getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Set<String> inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Set<String> getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Set<String> outputTokens) {
        this.outputTokens = outputTokens;
    }

    public String getFinalText() {
        return finalText;
    }

    public void setFinalText(String finalText) {
        this.finalText = finalText;
    }
}
