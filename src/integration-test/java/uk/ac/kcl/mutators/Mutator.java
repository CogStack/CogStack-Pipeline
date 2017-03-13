package uk.ac.kcl.mutators;

import org.junit.Ignore;

import java.util.Random;

/**
 * Created by rich on 05/07/16.
 */
@Ignore
public interface Mutator {
     Mutant mutate(String document);
}
