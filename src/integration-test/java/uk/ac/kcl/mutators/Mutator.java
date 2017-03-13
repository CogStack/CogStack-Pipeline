package uk.ac.kcl.mutators;

import java.util.Random;

/**
 * Created by rich on 05/07/16.
 */
public interface Mutator {
     Mutant mutate(String document);
}
