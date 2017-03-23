/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.kcl.mutators;


import de.svenjacobs.loremipsum.LoremIpsum;

import java.util.*;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

/**
 * @author rich
 */
@Ignore
@Service
@ComponentScan({"uk.ac.kcl"})
public class StringMutatorService {

    private final Random random = new Random();



    @Value("#{'${loremLength:10}'}")
    private int loremLength;


    @Autowired
    private SubstituteCharactersMutator substituteCharactersMutator;
    @Autowired
    private BadOCRMutator badOcrMutator;
    @Autowired
    private AddressAliasMutator addressAliasMutator;
    @Autowired
    private StringTokenTruncatorMutator stringTokenTruncatorMutator;
    @Autowired
    private NullMutator nullMutator;


    public int getLoremLength() {
        return loremLength;
    }

    public void setLoremLength(int loremLength) {
        this.loremLength = loremLength;
    }


    public Mutant generateMutantDocument(String[] stringsToMutate, String[] nonMutantStrings, int severity) {

        LoremIpsum loremIpsum = new LoremIpsum();
        int loremStart =0;
        int loremEnd =random.nextInt(loremLength);

        StringBuilder sb = new StringBuilder();
        sb.insert(0, loremIpsum.getWords(random.nextInt(loremLength)));
        Mutant parentMutant = new Mutant();
            for (int i = 0; i < stringsToMutate.length; i++) {
                Mutant childMutant = mutate(stringsToMutate[i], severity);
                parentMutant.getInputTokens().addAll(childMutant.getInputTokens());
                parentMutant.getOutputTokens().addAll(childMutant.getOutputTokens());
                sb.append(childMutant.getFinalText()).append(" ")
                        .append(loremIpsum.getWords(loremEnd,loremStart)).append(" ");
                loremStart = loremEnd +1;
                loremEnd = loremStart + random.nextInt(loremLength);
                if(loremEnd>49 ||loremStart>49){
                    loremStart =0;
                    loremEnd =random.nextInt(loremLength);
                }

                for(int j = 0;j<nonMutantStrings.length;j++){
                    sb.append(nonMutantStrings[j]).append(" ")
                            .append(loremIpsum.getWords(loremEnd,loremStart)).append(" ");
                    loremStart = loremEnd +1;
                    loremEnd = loremStart + random.nextInt(loremLength);
                    if(loremEnd>49 ||loremStart>49){
                        loremStart =0;
                        loremEnd =random.nextInt(loremLength);
                    }
                }
            }
        parentMutant.setFinalText(sb.toString());
        return parentMutant;
    }

    public Mutant generateMutantDocument(String[] stringToMutate, int severity) {

        LoremIpsum loremIpsum = new LoremIpsum();
        int loremStart =0;
        int loremEnd =random.nextInt(loremLength);

        StringBuilder sb = new StringBuilder();
        sb.insert(0, loremIpsum.getWords(random.nextInt(loremLength)));
        sb.append(" ");
        Mutant parentMutant = new Mutant();
        for (int i = 0; i < stringToMutate.length; i++) {
            Mutant childMutant = mutate(stringToMutate[i], severity);
            parentMutant.getInputTokens().addAll(childMutant.getInputTokens());
            parentMutant.getOutputTokens().addAll(childMutant.getOutputTokens());
            sb.append(childMutant.getFinalText()).append(" ")
                    .append(loremIpsum.getWords(loremEnd,loremStart)).append(" ");
            loremStart = loremEnd +1;
            loremEnd = loremStart + random.nextInt(loremLength);
        }
        parentMutant.setFinalText(sb.toString());
        return parentMutant;
    }


    public Mutant mutate(String normal, int severity) {
        Mutant mutant = null;
        switch (severity) {
            case 0:
                mutant = nullMutator.mutate(normal);
                break;
            case 1:
                mutant = substituteCharactersMutator.mutate(normal);
                break;
            case 2:
                mutant = addressAliasMutator.mutate(normal);
                break;
            case 3:
                mutant = stringTokenTruncatorMutator.mutate(normal);
                break;
            case 4:
                mutant = badOcrMutator.mutate(normal);
                break;
            case 5:
                int i = random.nextInt(4);
                if (i == 0) {
                    mutant = substituteCharactersMutator.mutate(normal);
                } else if (i == 1) {
                    mutant = addressAliasMutator.mutate(normal);
                } else if (i == 2) {
                    mutant = stringTokenTruncatorMutator.mutate(normal);
                } else if (i == 3) {
                    mutant = substituteCharactersMutator.mutate(normal);
                }
                break;
            default:
                mutant = nullMutator.mutate(normal);
                break;
        }
        return mutant;
    }








}