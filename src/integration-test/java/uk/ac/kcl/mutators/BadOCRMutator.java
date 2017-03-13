package uk.ac.kcl.mutators;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created by rich on 05/07/16.
 */
@Service
@Ignore
public class BadOCRMutator implements Mutator {
    private Random random = new Random();
    @Autowired
    SubstituteCharactersMutator substituteCharactersMutator;
    @Value("#{'${badOCRWhitespaceRate:3}'}")
    private int badOCRWhitespaceRate;

    @Value("#{'${badOCRCharacterMutationRate:3}'}")
    private int badOCRCharacterMutationRate;

    private Mutant simulateBadOCR(String normal) {
        substituteCharactersMutator.setMutationRate(badOCRCharacterMutationRate);
        StringTokenizer st = new StringTokenizer(normal);
        Mutant mutant = new Mutant();
        StringBuilder documentSB = new StringBuilder();

        while(st.hasMoreTokens()){
            StringBuilder tokenSb = new StringBuilder("");
            String token = st.nextToken();
            mutant.getInputTokens().add(token);
            String newToken = substituteCharactersMutator.mutate(token).getFinalText();
            char[] array = newToken.toCharArray();
            for (int i = 0; i < array.length; i++) {
                if (random.nextInt(100) <= badOCRWhitespaceRate) {
                    tokenSb.append(array[i]).append(" ");
                }else{
                    tokenSb.append(array[i]);
                }
            }
            StringTokenizer st2 = new StringTokenizer(tokenSb.toString());
            while (st2.hasMoreTokens()){
                mutant.getOutputTokens().add(st2.nextToken());
            }
            documentSB.append(" ").append(tokenSb.toString());
        }

        mutant.setFinalText(documentSB.toString());
        return mutant;
    }



    @Override
    public Mutant mutate(String document) {
        return simulateBadOCR(document);
    }
}
