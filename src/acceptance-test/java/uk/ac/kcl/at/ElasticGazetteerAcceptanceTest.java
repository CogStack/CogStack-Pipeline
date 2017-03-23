/* 
 * Copyright 2016 King's College London, Richard Jackson <richgjackson@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.kcl.at;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import uk.ac.kcl.utils.DbmsTestUtils;
import uk.ac.kcl.utils.PostGresTestUtils;
import uk.ac.kcl.utils.SqlServerTestUtils;
import uk.ac.kcl.utils.TestUtils;
import uk.ac.kcl.mutators.Mutant;
import uk.ac.kcl.rowmappers.DocumentRowMapper;
import uk.ac.kcl.scheduling.SingleJobLauncher;
import uk.ac.kcl.service.ElasticGazetteerService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author rich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan("uk.ac.kcl.it")
@TestPropertySource({
        "classpath:postgres_test.properties",
        "classpath:postgres_db.properties",
        "classpath:jms.properties",
        "classpath:noScheduling.properties",
        "classpath:gate.properties",
        "classpath:deidentification.properties",
        "classpath:elasticsearch.properties",
        "classpath:elasticgazetteer_test.properties",
        "classpath:jobAndStep.properties"})
@ContextConfiguration(classes = {
        SingleJobLauncher.class,
        PostGresTestUtils.class,
        TestUtils.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles({"deid","localPartitioning","jdbc_in","elasticsearchRest","primaryKeyPartition","postgres"})
//@ActiveProfiles({"deid","basic","localPartitioning","jdbc","primaryKeyPartition","sqlserver"})
public class ElasticGazetteerAcceptanceTest {

    final static Logger logger = Logger.getLogger(ElasticGazetteerAcceptanceTest.class);



    @Autowired
    DocumentRowMapper documentRowMapper;

    @Autowired
    ElasticGazetteerService elasticGazetteerService;

    @Autowired
    TestUtils testUtils;

    @Autowired
    DbmsTestUtils dbmsTestUtils;

    @Autowired
    Environment env;

    @Value("#{'${mutatortype:0}'}")
    private int mutatortype;

    @Test
    public void deidentificationPerformanceTest() {
        dbmsTestUtils.createBasicInputTable();
        dbmsTestUtils.createBasicOutputTable();
        dbmsTestUtils.createDeIdInputTable();
        List<Mutant> mutants = testUtils.insertTestDataForDeidentification(env.getProperty("tblIdentifiers"),
                env.getProperty("tblInputDocs"), mutatortype,true);

        int totalTruePositives = 0;
        int totalFalsePositives = 0;
        int totalFalseNegatives = 0;

        for (Mutant mutant : mutants) {
            Set<Pattern> mutatedPatterns = new HashSet<>();
            mutant.setDeidentifiedString(elasticGazetteerService.deIdentifyString(mutant.getFinalText(),
                    String.valueOf(mutant.getDocumentid())));
            Set<String> set = new HashSet<>(mutant.getOutputTokens());
            mutatedPatterns.addAll(set.stream().map(string -> Pattern.compile(Pattern.quote(string), Pattern.CASE_INSENSITIVE)).collect(Collectors.toSet()));
            List<MatchResult> results = new ArrayList<>();
            for (Pattern pattern : mutatedPatterns) {
                Matcher matcher = pattern.matcher(mutant.getFinalText());
                while (matcher.find()) {
                    results.add(matcher.toMatchResult());
                }
            }

            int truePositives = getTruePositiveTokenCount(mutant);
            int falsePositives = getFalsePositiveTokenCount(mutant);
            int falseNegatives = getFalseNegativeTokenCount(mutant);

            System.out.println("Doc ID " + mutant.getDocumentid() + " has " + falseNegatives + " unmasked identifiers from a total of " + (falseNegatives + truePositives));
            System.out.println("Doc ID " + mutant.getDocumentid() + " has " + falsePositives + " inaccurately masked tokens from a total of " + (falsePositives + truePositives));
            System.out.println("TP: " + truePositives + " FP: " + falsePositives + " FN: " + falseNegatives);
            System.out.println("Doc ID precision " + calcPrecision(falsePositives, truePositives));
            System.out.println("Doc ID recall " + calcRecall(falseNegatives, truePositives));
            System.out.println(mutant.getDeidentifiedString());
            System.out.println(mutant.getFinalText());
            System.out.println(mutant.getInputTokens());
            System.out.println(mutant.getOutputTokens());
            System.out.println();
            if (env.getProperty("elasticgazetteerTestOutput") != null) {
                try {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(
                            new File(env.getProperty("elasticgazetteerTestOutput") + File.separator + mutant.getDocumentid())))) {
                        bw.write("Doc ID " + mutant.getDocumentid() + " has " + falseNegatives + " unmasked identifiers from a total of " + (falseNegatives + truePositives));
                        bw.newLine();
                        bw.write("Doc ID " + mutant.getDocumentid() + " has " + falsePositives + " inaccurately masked tokens from a total of " + (falsePositives + truePositives));
                        bw.newLine();
                        bw.write("TP: " + truePositives + " FP: " + falsePositives + " FN: " + falseNegatives);
                        bw.newLine();
                        bw.write("Doc ID precision " + calcPrecision(falsePositives, truePositives));
                        bw.newLine();
                        bw.write("Doc ID recall " + calcRecall(falseNegatives, truePositives));
                        bw.newLine();
                        bw.write(mutant.getDeidentifiedString());
                        bw.newLine();
                        bw.write(mutant.getFinalText());
                        bw.newLine();
                        bw.write(mutant.getInputTokens().toString());
                        bw.newLine();
                        bw.write(mutant.getOutputTokens().toString());

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            totalTruePositives += truePositives;
            totalFalsePositives += falsePositives;
            totalFalseNegatives += falseNegatives;
        }
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);

        System.out.println();
        System.out.println();
        System.out.println("THIS RUN TP: " + totalTruePositives + " FP: " + totalFalsePositives + " FN: " + totalFalseNegatives);
        System.out.println("Doc ID precision " + calcPrecision(totalFalsePositives, totalTruePositives));
        System.out.println("Doc ID recall " + calcRecall(totalFalseNegatives, totalTruePositives));
        System.out.println(totalTruePositives + " & " + totalFalsePositives + " & " +totalFalseNegatives + " & " + df.format(calcPrecision(totalFalsePositives, totalTruePositives))
                + " & " + df.format(calcRecall(totalFalseNegatives, totalTruePositives)) + " \\\\");


        if (env.getProperty("elasticgazetteerTestOutput") != null) {
            try {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(
                        new File(env.getProperty("elasticgazetteerTestOutput") + File.separator + "summary")))) {
                    bw.write("THIS RUN TP: " + totalTruePositives + " FP: " + totalFalsePositives + " FN: " + totalFalseNegatives);
                    bw.newLine();
                    bw.write("Doc ID precision " + calcPrecision(totalFalsePositives, totalTruePositives));
                    bw.newLine();
                    bw.write("Doc ID recall " + calcRecall(totalFalseNegatives, totalTruePositives));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private int getFalsePositiveTokenCount( Mutant mutant){
        int count = 0;
        Pattern mask = Pattern.compile("X+");
        List<MatchResult> results = new ArrayList<>();
        Matcher matcher = mask.matcher(mutant.getDeidentifiedString());
        while (matcher.find()){
            results.add(matcher.toMatchResult());
        }
        for(MatchResult result: results) {
            StringTokenizer tokenizer = new StringTokenizer(mutant.getFinalText().substring(result.start(),result.end()));
            ArrayList<String> arHits = new ArrayList<>();
            while (tokenizer.hasMoreTokens()){
                arHits.add(tokenizer.nextToken());
            }
            for(String hit : arHits){
                boolean isAnIdentifier = false;
                for(String token : mutant.getOutputTokens()) {
                    if (hit.matches(Pattern.quote(token))) {
                        isAnIdentifier = true;
                    }
                }
                if(!isAnIdentifier && !hit.equalsIgnoreCase("")&& !hit.equalsIgnoreCase("-")){
                    count++;
                }
            }
        }
        return count;
    }

    private int getTruePositiveTokenCount( Mutant mutant){
        int count = 0;
        Pattern mask = Pattern.compile("X+");
        List<MatchResult> results = new ArrayList<>();
        Matcher matcher = mask.matcher(mutant.getDeidentifiedString());
        while (matcher.find()){
            results.add(matcher.toMatchResult());
        }
        for(MatchResult result: results) {
            StringTokenizer tokenizer = new StringTokenizer(mutant.getFinalText().substring(result.start(),result.end()));
            ArrayList<String> arHits = new ArrayList<>();
            while (tokenizer.hasMoreTokens()){
                arHits.add(tokenizer.nextToken());
            }
            count = getHitCount(mutant, count, arHits);
        }
        return count;
    }

    private int getHitCount(Mutant mutant, int count, ArrayList<String> arHits) {
        for(String hit : arHits){
            boolean hitFound = false;
            for(String token : mutant.getOutputTokens()) {
                if (hit.matches(Pattern.quote(token))) {
                    hitFound = true;
                }
            }
            if(hitFound && !hit.equalsIgnoreCase("")&& !hit.equalsIgnoreCase("-")){
                count++;
            }
        }
        return count;
    }


    private int getFalseNegativeTokenCount( Mutant mutant){
        int count = 0;
        StringTokenizer tokenizer = new StringTokenizer(mutant.getDeidentifiedString());
        ArrayList<String> arHits = new ArrayList<>();
        while (tokenizer.hasMoreTokens()){
            arHits.add(tokenizer.nextToken());
        }
        count = getHitCount(mutant, count, arHits);
        return count;
    }

    private double calcPrecision(int falsePositives, int truePositives){
        return (((double)truePositives / ((double)falsePositives + (double)truePositives)))*100.0;
    }

    private double calcRecall(int falseNegative, int truePositives){
        return (((double)truePositives / ((double)falseNegative + (double)truePositives)))*100.0;
    }




}
