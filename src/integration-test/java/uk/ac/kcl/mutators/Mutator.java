package uk.ac.kcl.mutators;

import java.util.Random;

/**
 * Created by rich on 05/07/16.
 */
public interface Mutator {
     final Random random = new Random();
     Mutant mutate(String document);
}
