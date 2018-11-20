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
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.*;
import java.util.Arrays;

/**
 * Created by rich on 05/03/17.
 * Updated to support x-pack by jstuczyn on 04/07/17.
 */
@Service("esRestService")
@Profile("elasticsearchRest")
public class ESRestService {

    private static final Logger LOG = LoggerFactory.getLogger(uk.ac.kcl.service.ESRestService.class);


    // mandatory properties
    //
    @Value("${elasticsearch.cluster.host}")
    private String clusterMainHost;
    @Value("${elasticsearch.cluster.port}")
    private int clusterMainHostPort;

    // optional properties
    //
    // additional ES nodes provided as a list 'address1:port1,address2:port2',...
    @Value("${elasticsearch.cluster.extraNodes:#{null}}")
    private String extraNodes;

    @Value("${elasticsearch.index.name:default_index")
    private String indexName;
    @Value("${elasticsearch.type:doc}")
    private String typeName;

    @Value("${elasticsearch.cluster.name:elasticsearch")
    private String clusterName;
    @Value("${elasticsearch.connect.timeout:5000}")
    private long connTimeout;
    @Value("${elasticsearch.response.timeout:60000}")
    private int respTimeout;
    @Value("${elasticsearch.retry.timeout:60000}")
    private int retryTimeout;

    @Value("${elasticsearch.xpack.enabled:false}")
    private boolean securityEnabled;
    @Value("${elasticsearch.xpack.security.transport.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${elasticsearch.xpack.user:#{null}}")
    private String user;
    @Value("${elasticsearch.xpack.password:#{null}}")
    private String userPassword;
    @Value("${elasticsearch.xpack.ssl.keystore.path:#{null}}")
    private String sslKeyStorePath;
    @Value("${elasticsearch.xpack.ssl.keystore.password:#{null}}")
    private String keyStorePassword;
    @Value("${elasticsearch.xpack.ssl.truststore.path:#{null}}")
    private String sslTrustStorePath;
    @Value("${elasticsearch.xpack.ssl.truststore.password:#{null}}")
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

        List<Map.Entry<String, Integer>> hostsInfo = getParsedHosts();

        if (securityEnabled && sslEnabled) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(user, userPassword));

            List<HttpHost> nodes = hostsInfo.stream()
                    .map(x -> new HttpHost(x.getKey(), x.getValue(), "https"))
                    .collect(Collectors.toList());

            restClient = RestClient.builder(nodes.toArray(HttpHost[]::new))
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
        } else if (securityEnabled) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(user));

            List<HttpHost> nodes = hostsInfo.stream()
                    .map(x -> new HttpHost(x.getKey(), x.getValue()))
                    .collect(Collectors.toList());

            restClient = RestClient.builder(nodes.toArray(HttpHost[]::new))
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
        } else {
            List<HttpHost> nodes = hostsInfo.stream()
                    .map(x -> new HttpHost(x.getKey(), x.getValue(), "http"))
                    .collect(Collectors.toList());

            restClient = RestClient.builder(nodes.toArray(HttpHost[]::new))
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
        LOG.debug("ESRestService.destroy() called");
        restClient.close();
        LOG.debug("restClient.close() completed");
    }

    private List<Map.Entry<String, Integer>> getParsedHosts() {
        ArrayList<Map.Entry<String, Integer>> hostsInfo = new ArrayList<>();

        // add the main node
        hostsInfo.add(new SimpleEntry<>(clusterMainHost, clusterMainHostPort));

        // add the extra nodes if provided
        try {
            if (extraNodes != null && extraNodes.length() > 0) {
                String[] hosts = extraNodes.split(",");
                for (String h : hosts) {
                    // get the last index of ':' since the hosts can start with 'xxx://...'
                    int i = h.lastIndexOf(':');
                    String host = h.substring(0, i);
                    String port = h.substring(i+1);
                    hostsInfo.add(new SimpleEntry<>(host.trim(), Integer.parseInt(port)));
                }
            }
        } catch (Exception e) {
            LOG.error("Error parsing additional ES nodes");
            throw e;
        }

        return hostsInfo;
    }


    private SSLContext getSslContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");
            try (InputStream is = Files.newInputStream(new File(sslKeyStorePath).toPath())) {
                keyStore.load(is, keyStorePassword.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            KeyStore truststore = KeyStore.getInstance("jks");
            try (InputStream is = Files.newInputStream(new File(sslTrustStorePath).toPath())) {
                truststore.load(is, trustStorePassword.toCharArray());
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(truststore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;

        } catch (CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
