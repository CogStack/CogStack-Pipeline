package uk.ac.kcl.mutators;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.StringTokenizer;

/**
 * Created by rich on 05/07/16.
 */
@Service
public class NullMutator implements Mutator {






    @Override
    public Mutant mutate(String document) {
        StringTokenizer st = new StringTokenizer(document);
        Mutant mutant = new Mutant();
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            mutant.getInputTokens().add(new String(token));
            mutant.getOutputTokens().add(new String(token));
        }

        mutant.setFinalText(document);
        return mutant;
    }
}
