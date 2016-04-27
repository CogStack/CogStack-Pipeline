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

import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.ContentHandler;
import uk.ac.kcl.model.BinaryDocument;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.model.MultilineDocument;
import uk.ac.kcl.model.TextDocument;
import uk.ac.kcl.rowmappers.SimpleDocumentRowMapper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author rich
 */
public class DbLineFixerItemProcessor implements ItemProcessor<Document, Document> {

    private static final Logger logJdbcPath = Logger.getLogger(DbLineFixerItemProcessor.class);

    @Autowired
    @Qualifier("sourceDataSource")
    private DataSource ds;
    @Autowired
    public SimpleDocumentRowMapper simpleMapper;

    private JdbcTemplate template;

    @Resource
    Environment env;



    @PostConstruct
    public void init(){
        this.template = new JdbcTemplate(ds);
    }

    @Override
    public Document process(final Document doc) throws Exception {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(env.getProperty("lf.documentKeyName"))
                .append(", ")
                .append(env.getProperty("lf.lineKeyName"))
                .append(", ")
                .append(env.getProperty("lf.lineContents"))
                .append(" FROM ")
                .append(env.getProperty("lf.srcTableName"))
                .append(" WHERE ")
                .append(env.getProperty("lf.documentKeyName"))
                .append(" = '")
                .append(doc.getPrimaryKeyFieldValue())
                .append("' ORDER BY ")
                .append(env.getProperty("lf.lineKeyName"))
                .append(" DESC");

        List<MultilineDocument> docs = template.query(sql.toString(), simpleMapper);

        TreeMap<Integer, String> map = new TreeMap<>();
        for (MultilineDocument mldoc : docs) {
            map.put(Integer.valueOf(mldoc.getLineKey()), mldoc.getLineContents());
        }
        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            sb2.append(entry.getValue());
        }
        doc.setOutputData(sb2.toString());

        return doc;
    }


}
