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
package uk.ac.kcl.itemProcessors;

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.MultilineDocument;
import uk.ac.kcl.rowmappers.SimpleDocumentRowMapper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author rich
 */
@Profile("dBLineFixer")
@Service("dBLineFixerItemProcessor")
public class DbLineFixerItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DbLineFixerItemProcessor.class);

    @Autowired
    @Lazy
    @Qualifier("sourceDataSource")
    private DataSource ds;

    @Autowired
    public SimpleDocumentRowMapper simpleMapper;

    private JdbcTemplate template;

    @Resource
    Environment env;

    @Value("${lf.documentKeyName}")
    private String documentKeyName;
    @Value("${lf.lineKeyName}")
    private String lineKeyName;
    @Value("${lf.srcTableName}")
    private String srcTableName;
    @Value("${lf.lineContents}")
    private String lineContents;
    @Value("${lf.fieldName}")
    private String fieldName;


    @PostConstruct
    public void init(){
        this.template = new JdbcTemplate(ds);
        setFieldName(fieldName);
    }

    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        String sql = MessageFormat.format("SELECT {0}, {1}, {2} FROM {3} WHERE {0} = ''{4}'' " +
                " ORDER BY {5} DESC ",documentKeyName,lineKeyName,
                lineContents,srcTableName,doc.getPrimaryKeyFieldValue(),lineKeyName);

        List<MultilineDocument> docs = template.query(sql, simpleMapper);

        TreeMap<Integer, String> map = new TreeMap<>();
        for (MultilineDocument mldoc : docs) {
            map.put(Integer.valueOf(mldoc.getLineKey()), mldoc.getLineContents());
        }
        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            sb2.append(entry.getValue());
        }

        addField(doc,sb2.toString());
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }


}
