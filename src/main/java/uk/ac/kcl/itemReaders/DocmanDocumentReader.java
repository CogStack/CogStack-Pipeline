package uk.ac.kcl.itemReaders;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


/**
 * Created by rich on 20/04/16.
 */
@Service("docmanReader")
@Profile("docmanReader")
public class DocmanDocumentReader implements ItemReader<Document> {
    private static final Logger LOG = LoggerFactory.getLogger(DocmanDocumentReader.class);
    private DocmanDocumentReader() {}

    private String pathSuffix;
    private String typeName;

    @Autowired
    Environment env;

    @PostConstruct
    public void init()  {

    }
    @PreDestroy
    public void destroy(){

    }


    @Override
    public Document read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return null;
    }
}
