package uk.ac.kcl.itemHandlers;


import org.apache.poi.ss.formula.functions.T;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.shield.ShieldPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

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
