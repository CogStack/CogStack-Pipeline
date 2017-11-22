package uk.ac.kcl.itemWriters;


import org.apache.poi.ss.formula.functions.T;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.shield.ShieldPlugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;

import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * This method is deprecated as using Java REST Client is recommended over Native Java API
 */
@Service("esDocumentWriter")
@Profile("elasticsearch")
public class ElasticsearchDocumentWriter implements ItemWriter<Document> {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchDocumentWriter.class);

    private ElasticsearchDocumentWriter() {
    }


    @Value("${elasticsearch.index.name:#{null}}")
    private String indexName;

    @Value("${elasticsearch.type:#{null}}")
    private String typeName;

    @Value("${elasticsearch.xpack.enabled:false}")
    private boolean securityEnabled;

    @Value("${elasticsearch.cluster.name:#{null}}")
    private String clusterName;

    @Value("${elasticsearch.cluster.host:#{null}}")
    private String clusterHost;

    @Value("${elasticsearch.response.timeout:10000}")
    private long timeout;

    @Value("${elasticsearch.cluster.port:#{null}}")
    private int port;

    @Value("${elasticsearch.xpack.user:#{null}}")
    private String user;

    @Value("${elasticsearch.xpack.ssl.keystore.path:#{null}}")
    private String sslKeyStorePath;

    @Value("${elasticsearch.xpack.ssl.keystore.password:#{null}}")
    private String sslKeyStorePassword;

    @Value("${elasticsearch.xpack.ssl.truststore.path:#{null}}")
    private String sslTrustStorePath;

    @Value("${elasticsearch.xpack.ssl.truststore.password:#{null}}")
    private String trustStorePassword;

    @Value("${elasticsearch.xpack.security.transport.ssl.enabled:false}")
    private boolean sslEnabled;

    @Autowired
    Environment env;

    private Client client;

    @PostConstruct
    public void init() throws UnknownHostException {
        Settings settings;

        if (securityEnabled) {
            settings = Settings.builder()
                .put("cluster.name", clusterName)
                .put("xpack.security.transport.ssl.enabled", sslEnabled)
                .put("request.headers.X-Found-Cluster", clusterName)
                .put("xpack.security.user", user)
                .put("xpack.ssl.keystore.path", sslKeyStorePath)
                .put("xpack.ssl.keystore.password", sslKeyStorePassword)
                .put("xpack.ssl.truststore.path", sslTrustStorePath)
                .put("xpack.ssl.truststore.password", trustStorePassword)

                .build();
            client = new PreBuiltXPackTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(
                            clusterHost),
                            port));
        } else {
            settings = Settings.builder()
                    .put("cluster.name", clusterName).build();
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(
                            clusterHost),
                            port));
        }
    }

    @PreDestroy
    public void destroy() {
        client.close();
    }

    @Override
    public final void write(List<? extends Document> documents) throws Exception {
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        for (Document doc : documents) {
            XContentParser parser = null;
            parser = XContentFactory.xContent(XContentType.JSON)
                    .createParser(NamedXContentRegistry.EMPTY, doc.getOutputData().getBytes());
            parser.close();
            XContentBuilder builder = jsonBuilder().copyCurrentStructure(parser);


            IndexRequestBuilder request = client.prepareIndex(
                    indexName,
                    typeName).setSource(
                    builder);
            request.setId(doc.getPrimaryKeyFieldValue());
            bulkRequest.add(request);
        }
        //check that no nonessential processes failed
        if (documents.size() != 0) {
            BulkResponse response;
            response = bulkRequest.execute().actionGet(timeout);
            getResponses(response);
        }
    }

    private void getResponses(BulkResponse response) {
        if (response.hasFailures()) {

            for (int i = 0; i < response.getItems().length; i++) {
                //in bulk processing, retry all docs one by one. if one fails, log it. If the entire chunk fails,
                // raise an exception towards skip limit
                if (response.getItems().length == 1) {
                    LOG.warn("failed to index document: " + response.getItems()[i].getId() + " failure is: \n"
                            + response.getItems()[i].getFailureMessage());
                } else {
                    LOG.error("failed to index document: " + response.getItems()[i].getFailureMessage());
                    throw new ElasticsearchException("Bulk indexing request failed");
                }
            }
        }
        LOG.info("{} documents indexed into ElasticSearch in {} ms", response.getItems().length,
                response.getIngestTookInMillis());
    }

}
