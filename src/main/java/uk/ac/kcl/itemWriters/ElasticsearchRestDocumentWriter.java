package uk.ac.kcl.itemWriters;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;
import uk.ac.kcl.service.ESRestService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rich on 20/04/16.
 */
@Service("esRestDocumentWriter")
@Profile("elasticsearchRest")
@ComponentScan("uk.ac.kcl.service")
public class ElasticsearchRestDocumentWriter implements ItemWriter<Document> {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchRestDocumentWriter.class);

    private ElasticsearchRestDocumentWriter() {}



    @Autowired
    @Qualifier("esRestService")
    private ESRestService esRestService;

    @Value("${elasticsearch.index.name:#{null}}")
    private String indexName;

    @Value("${elasticsearch.type:#{null}}")
    private String typeName;

    @Override
    public final void write(List<? extends Document> documents) {
        long startTime = System.currentTimeMillis();
        HttpEntity entity = makeEntity(documents);
        try {
            esRestService.getRestClient().performRequest(
                    "POST",
                    "_bulk" ,
                    Collections.<String, String>emptyMap(),
                    //assume that the documents are stored in an entities array
                    entity);
            long endTime = System.currentTimeMillis();
            LOG.info("{} documents written in bulk by elastic search REST client;Time:{} ms",
                     documents.size(), endTime - startTime);
        } catch (IOException e) {
            LOG.warn("IOException in ElasticsearchRestDocumentWriter.write()", e);
            throw new RuntimeException("Indexing failed:", e);
        }
    }

    private HttpEntity makeEntity(List<? extends Document> documents) {
        StringBuilder sb = new StringBuilder("");
        for (Document doc : documents){
            sb.append("{ \"index\" : { \"_index\" : \"").append(indexName).append("\" ,\"_type\" : \"")
                    .append(typeName).append("\" , \"_id\" : \"").append(doc.getPrimaryKeyFieldValue())
                    .append("\" }\n");
            sb.append(doc.getOutputData()).append("\n");
        }
        LOG.debug(sb.toString());
        return new NStringEntity(sb.toString(), ContentType.APPLICATION_JSON);
    }

}
