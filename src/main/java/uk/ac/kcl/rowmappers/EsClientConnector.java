package uk.ac.kcl.rowmappers;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by rich on 20/04/16.
 */
@Component
public class EsClientConnector {

    public EsClientConnector() {}

    @PostConstruct
    public void init() throws UnknownHostException {
//        Client client = TransportClient.builder().build()
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300))
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));
    }
}
