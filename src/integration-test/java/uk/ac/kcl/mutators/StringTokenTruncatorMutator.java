package uk.ac.kcl.mutators;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by rich on 05/07/16.
 */
@Service
public class StringTokenTruncatorMutator implements Mutator {


    private ImmutableMap<String, String> addressAbbrevMap;
    @Value("#{'${removeTokenRate:50}'}")
    private int removeTokenRate;
    @Value("#{'${minAddressTokenCount:3}'}")
    private int minTokenCount;
    @PostConstruct
    private void init() {

    }


    private Mutant removeTokensFromEnd(String normal) {
        Mutant mutant = new Mutant();
        mutant.getInputTokens().add(normal);
        StringTokenizer st = new StringTokenizer(normal);
        StringBuilder documentSB = new StringBuilder();
        int totalCount = st.countTokens();
        for (int i = 0; i < totalCount; i++) {
            if (i <= minTokenCount || i > minTokenCount && random.nextInt(100) <= removeTokenRate) {
                documentSB.append(st.nextToken()).append(" ");
            } else {
                break;
            }
        }
        mutant.getOutputTokens().add(documentSB.toString());
        mutant.setFinalText(documentSB.toString());
        return mutant;
    }


    @Override
    public Mutant mutate(String document) {
        return removeTokensFromEnd(document);
    }
}
