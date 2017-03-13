package uk.ac.kcl.service;

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
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.itemWriters.ElasticsearchRestDocumentWriter;
import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;

/**
 * Created by rich on 05/03/17.
 */
@Service("esRestService")
@Profile("elasticsearchRest")
public class ESRestService {


    private static final Logger LOG = LoggerFactory.getLogger(uk.ac.kcl.service.ESRestService.class);





    @Value("${elasticsearch.index.name:#{null}}")
    private String indexName;

    @Value("${elasticsearch.type:#{null}}")
    private String typeName;

    @Value("${elasticsearch.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${elasticsearch.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${elasticsearch.cluster.name:#{null}}")
    private String clusterName;

    @Value("${elasticsearch.cluster.host:#{null}}")
    private String clusterHost;

    @Value("${elasticsearch.connect.timeout:5000}")
    private long connTimeout;

    @Value("${elasticsearch.response.timeout:60000}")
    private int respTimeout;

    @Value("${elasticsearch.retry.timeout:60000}")
    private int retryTimeout;

    @Value("${elasticsearch.cluster.port:#{null}}")
    private int port;

    @Value("${elasticsearch.security.user:#{null}}")
    private String user;

    @Value("${elasticsearch.security.password:#{null}}")
    private String userPassword;

    @Value("${elasticsearch.shield.ssl.keystore.path:#{null}}")
    private String sslKeyStorePath;

    @Value("${elasticsearch.shield.ssl.keystore.password:#{null}}")
    private String keyStorePassword;

    @Value("${elasticsearch.shield.ssl.truststore.path:#{null}}")
    private String sslTrustStorePath;

    @Value("${elasticsearch.shield.ssl.truststore.password:#{null}}")
    private String trustStorePassword;

    @Autowired
    Environment env;

    public RestClient getRestClient() {
        return restClient;
    }

    private RestClient restClient;
    private CredentialsProvider credentialsProvider;

    @PostConstruct
    public void init() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {

        if (securityEnabled && sslEnabled) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(user, userPassword));

            restClient = RestClient.builder(new HttpHost(clusterHost,port))
                    .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                        @Override
                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                            return httpClientBuilder
                                    .setDefaultCredentialsProvider(credentialsProvider)
                                    .setSSLContext(getSslContext());

                        }
                    })
                    .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                        @Override
                        public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                            return requestConfigBuilder.setConnectTimeout(respTimeout)
                                    .setSocketTimeout(retryTimeout);
                        }
                    })
                    .build();
        } else if(securityEnabled){
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(user, userPassword));

            restClient = RestClient.builder(new HttpHost(clusterHost,port))
                    .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                        @Override
                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                            return httpClientBuilder
                                    .setDefaultCredentialsProvider(credentialsProvider);

                        }
                    })
                    .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                        @Override
                        public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                            return requestConfigBuilder.setConnectTimeout(respTimeout)
                                    .setSocketTimeout(retryTimeout);
                        }
                    })
                    .build();
        }else {
            restClient = RestClient.builder(new HttpHost(clusterHost, port, "http"))
                    .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                        @Override
                        public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                            return requestConfigBuilder.setConnectTimeout(respTimeout)
                                    .setSocketTimeout(retryTimeout);
                        }
                    })
                    .setMaxRetryTimeoutMillis(retryTimeout)
                    .build();
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        restClient.close();
    }


    private SSLContext getSslContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");
            try (InputStream is = Files.newInputStream(new File(sslKeyStorePath).toPath())) {
                keyStore.load(is, keyStorePassword.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            KeyStore truststore = KeyStore.getInstance("jks");
            try (InputStream is = Files.newInputStream(new File(sslTrustStorePath).toPath())) {
                truststore.load(is, trustStorePassword.toCharArray());
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(truststore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        }catch (CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}



