package uk.ac.kcl.mutators;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.kcl.it.MagicSquare;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created by rich on 05/07/16.
 */
@Service
public class SubstituteCharactersMutator implements Mutator {

    private Map<Character, Integer> charTotals;
    @Autowired
    private MagicSquare ms;

    public void setMutationRate(int mutationRate) {
        this.mutationRate = mutationRate;
    }

    @Value("#{'${humanMistypeMutationRate:8}'}")
    private int mutationRate;
    private ImmutableMap<Integer, Character> characterMap;

    private HashMap<Character, Integer> characterMapKeys;


    @PostConstruct
    public void init(){
        this.characterMap = ImmutableMap.<Integer, Character>builder()
                .put(0, "A".charAt(0)).put(1, "B".charAt(0)).put(2, "C".charAt(0)).put(3, "D".charAt(0)).put(4, "E"
                        .charAt(0)).put(5, "F".charAt(0)).put(6, "G".charAt(0)).put(7, "H".charAt(0)).put(8, "I"
                        .charAt(0)).put(9, "J".charAt(0)).put(10, "K".charAt(0)).put(11, "L".charAt(0)).put(12, "M"
                        .charAt(0)).put(13, "N".charAt(0)).put(14, "O".charAt(0)).put(15, "P".charAt(0)).put(16, "Q"
                        .charAt(0)).put(17, "R".charAt(0)).put(18, "S".charAt(0)).put(19, "T".charAt(0)).put(20, "U"
                        .charAt(0)).put(21, "V".charAt(0)).put(22, "W".charAt(0)).put(23, "X".charAt(0)).put(24, "Y"
                        .charAt(0)).put(25, "Z".charAt(0)).build();
        this.characterMapKeys = new HashMap<>();
        for (Map.Entry<Integer, Character> entry : characterMap.entrySet()) {
            characterMapKeys.put(entry.getValue(), entry.getKey());
        }
        charTotals = new HashMap<>();
        int[][] matrix = ms.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            int total = 0;
            for (int j = 0; j < matrix[i].length; j++) {
                total = total + matrix[i][j];
            }
            charTotals.put(characterMap.get(i), total);
        }
    }

    private String getMutation(char c, int hit) {

        String returnC = "";
        int i = random.nextInt(3);
        int[][] matrix = ms.getMatrix();
        int cursor = 0;
        int j;
        for (j = 0; j < matrix[characterMapKeys.get(c)].length; j++) {
            cursor = cursor + matrix[characterMapKeys.get(c)][j];
            if (cursor >= hit) {
                break;
            }
        }
        if (i == 0) {
            returnC = "";
        } else if (i == 1) {
            returnC = " ";
        } else {
            returnC = c + characterMap.get(j).toString();
        }
        return returnC;
    }
    private Mutant subCharacters(String normal, int mutationRate) {
        StringTokenizer st = new StringTokenizer(normal);
        Mutant mutant = new Mutant();
        StringBuilder documentSB = new StringBuilder();
        while(st.hasMoreTokens()){
            StringBuilder tokenSb = new StringBuilder("");
            String token = st.nextToken();
            mutant.getInputTokens().add(token);
            char[] array = token.toCharArray();
            for (int i = 0; i < array.length; i++) {
                if (random.nextInt(100) <= mutationRate) {
                    if (characterMapKeys.containsKey(array[i])) {
                        char a = array[i];
                        int b = charTotals.get(array[i]);
                        int c = random.nextInt(b);
                        String mutation =getMutation(a, c);
                        tokenSb.append(mutation);
                    }
                } else {
                    tokenSb.append(array[i]);
                }
            }
            mutant.getOutputTokens().add(tokenSb.toString());
            documentSB.append(" ").append(tokenSb.toString());
        }
        mutant.setFinalText(documentSB.toString());
        return mutant;
    }

    @Override
    public Mutant mutate(String document){
        return subCharacters(document, mutationRate);
    }
}
