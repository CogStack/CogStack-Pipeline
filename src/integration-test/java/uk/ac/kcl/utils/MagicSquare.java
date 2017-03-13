/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.kcl.utils;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class MagicSquare {

    private int[][] matrix;

    private int size = -1;
    private int log10 = 0;
    private String numberFormat;

//    public ReadMagicSquare(String filename) {
//
//    }

    @PostConstruct
    public void readFile() throws IOException {
        // If included in an Eclipse project.
        InputStream stream = ClassLoader.getSystemResourceAsStream("magic-square.csv");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));

        // If in the same directory - Probably in your case...
        // Just comment out the 2 lines above this and uncomment the line
        // that follows.
        //BufferedReader buffer = new BufferedReader(new FileReader(filename));

        String line;
        int row = 0;

        while ((line = buffer.readLine()) != null) {
            String[] vals = line.trim().split("\\s+");

            // Lazy instantiation.
            if (matrix == null) {
                size = vals.length;
                matrix = new int[size][size];
                log10 = (int) Math.floor(Math.log10(size * size)) + 1;
                numberFormat = String.format("%%%dd", log10);
            }

            for (int col = 0; col < size; col++) {
                matrix[row][col] = Integer.parseInt(vals[col]);
            }

            row++;
        }
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        if (matrix != null) {
            for (int row = 0; row < size; row++) {
                buff.append(" ");
                for (int col = 0; col < size; col++) {
                    buff.append(String.format(numberFormat,  matrix[row][col]));
                    if (col < size - 1) {
                        buff.append(" | ");
                    }
                }
                if (row < size - 1) {
                    buff.append("\n");
                    for (int col = 0; col < size; col++) {
                        for (int i = 0; i <= log10 + 1; i++) {
                            buff.append("-");
                        }
                        if (col < size - 1) {
                            buff.append("+");
                        }
                    }
                    buff.append("\n");
                } else {
                    buff.append("\n");
                }
            }
        }

        return buff.toString();
    }
    public int[][] getMatrix() {
        return matrix;
    }
}