package uk.ac.kcl.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ElasticGazetteerService.class);
    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    @Lazy
    @Qualifier("sourceDataSource")
    DataSource sourceDataSource;

    @Autowired
    private Environment env;

    private JdbcTemplate jdbcTemplate;
    private  List<String> datePatterns;

    // mandatory properties
    @Value("${deid.stringTermsSQLFront}")
    private String FrontSQLTermForStringValue;
    @Value("${deid.timestampTermsSQLFront}")
    private String FrontSQLTermForTimestampValue;

    // optional properties with default values
    @Value("${deid.stringTermsSQLBack:#{null}}")
    private String BackSQLTermForStringValue;
    @Value("${deid.timestampTermsSQLBack:#{null}}")
    private String BackSQLTermForTimestampValue;

    @Value("${deid.levDistance:30}")
    private Integer levDistance;
    @Value("${deid.minWordLength:3}")
    private Integer minWordLength;


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
        Set<Pattern> patterns;
        List<String> strings = getStrings(docPrimaryKey);
        patterns = getStringPatterns(strings,document);
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

    private Set<Pattern> getStringPatterns(List<String> strings, String document) {


        Set<String> stringSet = new HashSet<>();
        for(String string : strings) {
            stringSet.add(string);
            stringSet.addAll(StringTools.getApproximatelyMatchingStringList(document, string,levDistance));
            stringSet.addAll((StringTools.splitIntoWordsWithLengthHigherThan(
                    string, minWordLength)));
        }

        Set<Pattern> patterns = new HashSet<>();
        patterns.addAll(stringSet.stream().map(string -> Pattern.compile(Pattern.quote(string),
                Pattern.CASE_INSENSITIVE)).collect(Collectors.toSet()));

        return patterns;
    }


    private List<Pattern> getTimestampPatterns(List<Timestamp> timestamps) {
        List<Pattern> patterns = new ArrayList<>();
        for(Timestamp ts : timestamps) {
            for(String date: datePatterns){
                SimpleDateFormat dateFormat = new SimpleDateFormat(date);
                try {
                    patterns.add(Pattern.compile(dateFormat.format(ts)));
                }catch (NullPointerException e){
                    LOG.debug("null detected in input");
                };
            }
        }
        return patterns;

    }
    private List<String> getStrings(String docPrimaryKey){
        String sql = FrontSQLTermForStringValue + " '" + docPrimaryKey + "' ";
        if (BackSQLTermForStringValue != null)
            sql += BackSQLTermForStringValue;
        LOG.info("Executing SQL query: " + sql);
        return jdbcTemplate.queryForList(sql, String.class);
    }
    private List<Timestamp> getTimestamps(String docPrimaryKey){
        String sql = FrontSQLTermForTimestampValue + " '" + docPrimaryKey + "' ";
        if (BackSQLTermForTimestampValue != null)
            sql += BackSQLTermForTimestampValue;
        LOG.info("Executing SQL query: " + sql);
        return jdbcTemplate.queryForList(sql, Timestamp.class);
    }
}
