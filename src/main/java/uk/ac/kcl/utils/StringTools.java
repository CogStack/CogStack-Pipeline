/*
        Cognition-DNC (Dynamic Name Concealer)         Developed by Ismail Kartoglu (https://github.com/iemre)
        Binary to text document converter and database pseudonymiser.

        Copyright (C) 2015 Biomedical Research Centre for Mental Health

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package uk.ac.kcl.utils;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringTools {

    private static List<String> maleNames = new ArrayList<>();
    private static List<String> femaleNames = new ArrayList<>();
    private static List<String> lastNames = new ArrayList<>();

    public static int getLevenshteinDistance(String str1, String str2) {
        return StringUtils.getLevenshteinDistance(str1, str2);
    }


    public static Set<String> getApproximatelyMatchingStringList(String sourceString, String search) {
        return getApproximatelyMatchingStringList(sourceString, search, getMaxAllowedLevenshteinDistanceFor(search));
    }

    public static boolean isNotTooShort(String string) {
        if (StringUtils.isBlank(string)) {
            return false;
        }

        return string.trim().length() > 3;
    }

    /**
     * @param sourceString Source string to search for approximately matching segments.
     * @param search String to search in {@code sourceString}.
     * @param maxDistance Maximum edit distance that should be satisfied.
     * @return A list of substrings from the @sourceString each of which approximately matches {@code search}.
     */
    public static Set<String> getApproximatelyMatchingStringList(String sourceString, String search, int maxDistance) {
        Set<String> matches = new HashSet<>();
        if (StringUtils.isBlank(search)) {
            return matches;
        }
        search = search.trim();

        int searchLength = search.length();
        if (searchLength <= 1) {
            return matches;
        }
        if (searchLength <= 3) {
            matches.add(search);
            return matches;
        }
        sourceString = sourceString.toLowerCase().trim();
        search = search.toLowerCase().trim();
        for (int i = 0; i < sourceString.length(); i++) {
            int endIndex = i + searchLength;
            if (endIndex >= sourceString.length()) {
                endIndex = sourceString.length();
            }
            String completingString = getCompletingString(sourceString, i, endIndex);
            if (matches.contains(completingString)) {
                continue;
            }
            if (getLevenshteinDistance(completingString, search) <= maxDistance) {
                //handled with Pattern.quote
                //matches.add(completingString.replace("\"", "\\\""));

                matches.add(completingString);
                i = endIndex;
            }
        }
        return matches;
    }

    /**
     * @param word
     * @return Max heuristic Levenshtein distance for {@code word}.
     */
    protected static int getMaxAllowedLevenshteinDistanceFor(String word) {
        if (StringUtils.isBlank(word)) {
            return 0;
        }
        return Math.round((float)word.length()*15/100);
    }

    public static String getCompletingString(String string, int begin, int end) {
        while ( begin > 0 && StringUtils.isAlphanumeric(string.substring(begin, begin+1)) ){
            begin -= 1;
        }
        if (begin != 0)
            begin += 1;

        while ( end < string.length() - 1 && StringUtils.isAlphanumeric(string.substring(end, end + 1)) ){
            end += 1;
        }

        String regex = "\\w+(\\(?\\)?\\s+\\w+)*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string.substring(begin, end));

        if (matcher.find()) {
            return matcher.group();
        }

        return StringUtils.EMPTY;
    }

    /**
     *
     * @param text The source text.
     * @param search The text to be searched in {@code text}.
     * @param threshold The threshold value between 0.0 - 1.0.
     * @return A list of MatchingWindow objects.
     */
    public static List<MatchingWindow> getMatchingWindowsAboveThreshold(String text, String search, double threshold) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        if (StringUtils.isBlank(search)) {
            return new ArrayList<>();
        }
        String[] addressWords = search.split(" ");
        int bagSize = addressWords.length;
        String[] textWords = text.split(" ");
        int textWordCount = textWords.length;
        List<MatchingWindow> windows = new ArrayList<>();
        for (int i = 0; i < textWordCount; i++) {
            MatchingWindow window = takeBag(textWords, i, bagSize);
            window.setScoreAccordingTo(addressWords);
            window.setMatchingText(text.substring(window.getBegin(), window.getEnd()));
            windows.add(window);
        }

        Collections.sort(windows);
        windows = windows.stream().filter(window -> window.isScoreAboveThreshold(threshold)).collect(Collectors.toList());

        return windows;
    }

    private static MatchingWindow takeBag(String[] textWords, int startWordIndex, int bagSize) {
        MatchingWindow window = new MatchingWindow();
        int offset = 0;
        for (int i = startWordIndex; i < startWordIndex+bagSize; i++) {
            if (i >= textWords.length) {
                break;
            }
            offset += textWords[i].length() + 1;
            window.addWord(textWords[i]);
        }
        offset -= 1;
        int begin = 0;
        for (int i = 0; i < startWordIndex; i++) {
            begin += textWords[i].length() + 1;
        }
        window.setBegin(begin);
        window.setEnd(begin + offset);

        return window;
    }

    /**
     * Splits the given string into words and returns a set of those words that have a greater
     * length than the argument {@code minLength}.
     * @param string String to be split.
     * @param minLength Minimum allowed length of a word.
     * @return A set of words with length larger than {@code minLength}.
     */
    public static Set<String> splitIntoWordsWithLengthHigherThan(String string, int minLength) {
        Set<String> strings = new HashSet<>();

        if (StringUtils.isBlank(string)) {
            return strings;
        }

        String[] splitArray = string.split(" ");
        for (String word : splitArray) {
            if (word.length() > minLength) {
                strings.add(word);
            }
        }
        return strings;
    }

    public static Set<String> splitIntoWordsWithLengthHigherThan(String string, int minLength, String... ignoreWords) {
        if (StringUtils.isBlank(string)) {
            return new HashSet<>();
        }
        for (String ignoreWord : ignoreWords) {
            string = string.replaceAll("(?i)"+ignoreWord, "");
        }
        return splitIntoWordsWithLengthHigherThan(string, minLength);
    }

    public static boolean noContentInHtml(String text) {
        if (StringUtils.isBlank(text)) {
            return true;
        }
        try {
            Document doc = Jsoup.parse(text);

            String bodyText = doc.body().text();

            return StringUtils.isBlank(bodyText);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * @param text
     * @param regex
     * @param minLength
     * @return  Returns a list of strings with minimum length of {@code minLength}
     * that match the given regular expression.
     */
    public static List<String> getRegexMatchesWithMinLength(String text, String regex, int minLength) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String match = matcher.group();
            if (match.length() >= minLength) {
                result.add(match);
            }
        }
        return result;
    }

    public static String getRandomForeName() {
        if (CollectionUtils.isEmpty(maleNames)) {
            loadMaleNames();
            loadFemaleNames();
            loadSurnames();
        }


        boolean male = RandomUtils.nextBoolean();
        if (male) {
            int randIndex = RandomUtils.nextInt(maleNames.size());
            return maleNames.get(randIndex);
        }

        int randIndex = RandomUtils.nextInt(femaleNames.size());
        return femaleNames.get(randIndex);
    }

    public static String getRandomSurname() {
        if (CollectionUtils.isEmpty(lastNames)) {
            loadSurnames();
        }

        int randIndex = RandomUtils.nextInt(lastNames.size());
        return lastNames.get(randIndex);
    }

    private static void loadSurnames() {
        InputStream resourceAsStream = StringTools.class.getClassLoader().getResourceAsStream("anonymisation/lastnames");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                lastNames.add(WordUtils.capitalize(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadFemaleNames() {
        InputStream resourceAsStream = StringTools.class.getClassLoader().getResourceAsStream("anonymisation/femaleNames");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                femaleNames.add(WordUtils.capitalize(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadMaleNames() {
        InputStream resourceAsStream = StringTools.class.getClassLoader().getResourceAsStream("anonymisation/maleNames");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                maleNames.add(WordUtils.capitalize(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}