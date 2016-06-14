package uk.ac.kcl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.utils.StringTools;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by rich on 06/06/16.
 */
@Service
@Profile("deid")
public class ElasticGazetteerService {


    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("sourceDataSource")
    DataSource sourceDataSource;

    @Autowired
    private Environment env;

    private JdbcTemplate jdbcTemplate;
    private  List<String> datePatterns;

    @PostConstruct
    private void init() throws IOException {

        this.jdbcTemplate = new JdbcTemplate(sourceDataSource);
        Resource datePatternsResource = resourceLoader.getResource("classpath:datePatterns.txt");
        this.datePatterns = new ArrayList<>();
        InputStream inputStream = datePatternsResource.getInputStream();


        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
            String line;
            while ((line = br.readLine()) != null) {
                this.datePatterns.add(line);
            }
        }

    }



    public String deIdentifyDates(String document, String docPrimaryKey){
        List<Pattern> patterns;
        List<Timestamp> timestamps = getTimestamps(docPrimaryKey);
        patterns = getTimestampPatterns(timestamps);
        List<MatchResult> results = new ArrayList<>();
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(document);
            while (matcher.find()){
                results.add(matcher.toMatchResult());
            }
        }
        return replaceStrings(results, document);
    }




    public String deIdentifyString(String document, String docPrimaryKey){
        double levDistance = Double.valueOf(env.getProperty("levDistance"));
        List<Pattern> patterns;
        List<String> strings = getStrings(docPrimaryKey);
        patterns = getStringPatterns(strings,document,levDistance);
        String str2="";
        List<MatchResult> results = new ArrayList<>();
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(document);
            while (matcher.find()){
                results.add(matcher.toMatchResult());

            }
        }
        return replaceStrings(results, document);
    }

    private String replaceStrings(List<MatchResult> results, String document) {
        StringBuffer sb = new StringBuffer(document);
        for(MatchResult m : results) {
            int startOffset =m.start();
            int endOffset = m.end();
            StringBuffer outputBuffer = new StringBuffer();
            for (int i = 0; i < (endOffset - startOffset); i++) {
                outputBuffer.append("X");
            }
            sb.replace(startOffset, endOffset, outputBuffer.toString());
        }
        return sb.toString();
    }

    private List<Pattern> getStringPatterns(List<String> strings, String document, double levDistance) {

        List<Pattern> patterns = new ArrayList<>();

        for(String string : strings) {
            patterns.addAll(StringTools.getApproximatelyMatchingStringList(document, string).stream().map(approximateMatch -> Pattern.compile(Pattern.quote(approximateMatch), Pattern.CASE_INSENSITIVE)).collect(Collectors.toList()));
            patterns.addAll(StringTools.getMatchingWindowsAboveThreshold(document, string, levDistance).stream().filter(window -> StringTools.isNotTooShort(string)).map(window -> Pattern.compile(Pattern.quote(window.getMatchingText()), Pattern.CASE_INSENSITIVE)).collect(Collectors.toList()));
            patterns.addAll(StringTools.splitIntoWordsWithLengthHigherThan(string, Integer.valueOf(env.getProperty("minWordLength"))).stream().map(word -> Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE)).collect(Collectors.toList()));
        }
        return patterns;
    }

    private List<Pattern> getTimestampPatterns(List<Timestamp> timestamps) {
        List<Pattern> patterns = new ArrayList<>();
        for(Timestamp ts : timestamps) {
            for(String date: datePatterns){
                SimpleDateFormat dateFormat = new SimpleDateFormat(date);
                patterns.add(Pattern.compile(dateFormat.format(ts)));
            }
        }
        return patterns;

    }
    private List<String> getStrings(String docPrimaryKey){
        String sql = env.getProperty("stringTermsSQLFront");
        sql = sql + " '" + docPrimaryKey + "' ";
        sql = sql + env.getProperty("stringTermsSQLBack");
        return jdbcTemplate.queryForList(sql, String.class);
    }
    private List<Timestamp> getTimestamps(String docPrimaryKey){
        String sql = env.getProperty("timestampTermsSQLFront");
        sql = sql + " '" + docPrimaryKey + "' ";
        sql = sql + env.getProperty("timestampTermsSQLBack");
        return jdbcTemplate.queryForList(sql, Timestamp.class);
    }
}
