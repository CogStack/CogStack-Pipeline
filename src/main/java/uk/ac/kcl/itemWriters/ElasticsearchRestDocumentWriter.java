package uk.ac.kcl.itemWriters;

import com.google.gson.*;
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

import org.apache.http.util.EntityUtils;


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
            Response response = esRestService.getRestClient().performRequest(
                    "POST",
                    "_bulk" ,
                    Collections.<String, String>emptyMap(),
                    //assume that the documents are stored in an entities array
                    entity);
            long endTime = System.currentTimeMillis();

            String docIdStart = documents.get(0).getPrimaryKeyFieldValue();
            String docIdEnd = documents.get(documents.size()-1).getPrimaryKeyFieldValue();
            checkResponse(response, docIdStart,docIdEnd, endTime - startTime);
        } catch (IOException e) {
            LOG.warn("IOException in ElasticsearchRestDocumentWriter.write()", e);
            throw new RuntimeException("Indexing failed:", e);
        }
    }

    private HttpEntity makeEntity(List<? extends Document> documents) {
        StringBuilder sb = new StringBuilder("");
        for (Document doc : documents){
            // INFO: uses "index" directive, which will:
            // - create a new document when none exists under a given ID
            // - update the document when the same exists with a given ID
            sb.append("{ \"index\" : { \"_index\" : \"")
                    .append(indexName).append("\" ,\"_type\" : \"")
                    .append(typeName).append("\" , \"_id\" : \"")
                    .append(doc.getPrimaryKeyFieldValue()).append("\" }\n");
            sb.append(doc.getOutputData()).append("\n");
        }
        LOG.debug(sb.toString());
        return new NStringEntity(sb.toString(), ContentType.APPLICATION_JSON);
    }

    private void checkResponse(Response response, String docIdStart, String docIdEnd, long processingTime) {
        try {
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

            // begin parsing of the JSON content of the response
            JsonElement jsonResponse = new JsonParser().parse(responseString);
            assert (jsonResponse.isJsonObject());

            JsonObject jsonResponseObject = jsonResponse.getAsJsonObject();

            // the "errors" entity is always present in the response from ES REST API
            JsonPrimitive jsonErrorStatus = jsonResponseObject.getAsJsonPrimitive("errors");
            String errorStatus = jsonErrorStatus.getAsString();

            JsonArray jsonItemsArray = jsonResponseObject.getAsJsonArray("items");

            // check whether an error occurred
            //
            if (errorStatus == "false") {
                // no errors -- just inform about the number of documents written
                LOG.info("{} documents written successfully in bulk by elastic search REST client ; " +
                                "doc_id_rage: {} - {} ; processing_time: {} ms",
                        jsonItemsArray.size(), docIdStart, docIdEnd, processingTime);
            } else {
                // on errors -- check which documents failed and print their ids with error message
                LOG.warn("{} documents written with errors in bulk by elastic search REST client ; " +
                                "doc_id_range: {} - {} ; processing_time: {} ms",
                        jsonItemsArray.size(), docIdStart, docIdEnd, processingTime);

                for (JsonElement jsonItem : jsonItemsArray) {
                    assert (jsonItem.isJsonObject());

                    JsonElement jsonCreateElem = jsonItem.getAsJsonObject().get("create");
                    assert (jsonCreateElem.isJsonObject());

                    JsonObject jsonCreateObject = jsonCreateElem.getAsJsonObject();

                    if (jsonCreateObject.has("error")) {
                        JsonPrimitive statusCode = jsonCreateObject.getAsJsonPrimitive("status");

                        String itemId = jsonCreateObject.getAsJsonPrimitive("_id").getAsString();

                        JsonElement jsonErrorElem = jsonCreateObject.get("error");
                        assert (jsonErrorElem.isJsonObject());

                        JsonObject jsonErrorObject = jsonErrorElem.getAsJsonObject();

                        String errorReason = jsonErrorObject.getAsJsonPrimitive("reason").getAsString();
                        LOG.warn("Failed writing document to ES -- doc_id: {} ; status_code: {} ; reason: \"{}\"", itemId, statusCode, errorReason);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Error parsing ES REST response: " + e.getMessage());
        }
    }

}
