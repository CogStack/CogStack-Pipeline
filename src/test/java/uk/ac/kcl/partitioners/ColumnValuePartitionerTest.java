package uk.ac.kcl.partitioners;

import org.junit.Test;


import java.util.*;


/**
 * Created by rich on 15/04/16.
 */
public class ColumnValuePartitionerTest {


    @Test
    public void partition() throws Exception {


    }
    @Test
    public void testSimpleInt() {

        int gridSize = 100;
        ArrayList<String> keyAR = new ArrayList<>();
        for (int i = 1; i < 1333; i++) {
            keyAR.add(String.valueOf(i));
        }
        Iterator<String> keysIter = keyAR.iterator();
        long keyCount = keyAR.size();
        System.out.println("Total Keys = " + keyCount);
        long targetSize = keyCount / gridSize +1;
        System.out.println("keys per Partition = " + targetSize);


        int ii = 1;
        while(keysIter.hasNext()){
            System.out.println("new partition " + ii);
            ArrayList<String> arr = new ArrayList<>();
            for (int i = 0; i < targetSize; i++) {
                arr.add(keysIter.next());
                if (!keysIter.hasNext()) {
                    break;
                }

            }
            ii++;
            System.out.println("partition contains " + Arrays.deepToString(arr.toArray()));
        }
    }
}