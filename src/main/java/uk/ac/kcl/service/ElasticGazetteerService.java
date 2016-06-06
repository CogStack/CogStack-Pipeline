package uk.ac.kcl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.utils.MatchingWindow;
import uk.ac.kcl.utils.StringTools;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rich on 06/06/16.
 */
@Service
@Profile("deid")
public class ElasticGazetteerService {


    @Autowired
    @Qualifier("sourceDataSource")
    DataSource sourceDataSource;

    @Autowired
    private Environment env;

    JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void init(){
        this.jdbcTemplate = new JdbcTemplate(sourceDataSource);
    }

    public String deIdentify(String document, String docPrimaryKey){
        double levDistance = Double.valueOf(env.getProperty("levDistance"));
        List<Pattern> patterns = null;
        List<String> strings = getStrings(docPrimaryKey);
        patterns = getPatterns(strings,document,levDistance);
        String str2="";
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(document);
            while (matcher.find()){
                str2 = matcher.replaceAll(" XXXXX ");

            }

        }
        return str2;
    }

    private List<Pattern> getPatterns(List<String> strings, String document, double levDistance) {

        List<Pattern> patterns = new ArrayList<>();

        for(String string : strings) {
            for (String approximateMatch : StringTools.getApproximatelyMatchingStringList(document, string)){
                patterns.add(Pattern.compile(Pattern.quote(approximateMatch)));
            }
            for ( MatchingWindow window : StringTools.getMatchingWindowsAboveThreshold(document, string, levDistance) ) {
                if (StringTools.isNotTooShort(string)){
                    patterns.add(Pattern.compile(Pattern.quote(window.getMatchingText())));
                }
            }
            for ( String word : StringTools.splitIntoWordsWithLengthHigherThan(string, Integer.valueOf(env.getProperty("minWordLength")))) {
                patterns.add(Pattern.compile(Pattern.quote(word)));
            }
        }
        return patterns;
    }
    private List<String> getStrings(String docPrimaryKey){
        String sql = env.getProperty("termsSQLFront");
        sql = sql + " '" + docPrimaryKey + "' ";
        sql = sql + env.getProperty("termsSQLBack");
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
