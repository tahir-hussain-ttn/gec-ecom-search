package com.husain707.reactive.growth.ecom.search.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.support.HttpHeaders;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Configuration
public class ElasticsearchClientConfig extends ReactiveElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String[] uris;

    @Value("${spring.elasticsearch.username:#{null}}")
    private String username;

    @Value("${spring.elasticsearch.password:#{null}}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:5s}")
    private Duration connectTimeout;

    @Value("${spring.elasticsearch.socket-timeout:3s}")
    private Duration socketTimeout;

    @Value("${spring.elasticsearch.ssl.certificate-path}")
    private String certificatePath;

    @Override
    public ClientConfiguration clientConfiguration() {
        try {
            // Load the CA certificate from the path specified in properties
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            try (InputStream caInput = ResourceUtils.getURL(certificatePath).openStream()) {
                X509Certificate ca = (X509Certificate) cf.generateCertificate(caInput);

                // Create a KeyStore containing our trusted CAs
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                HttpHeaders defaultHeaders = new HttpHeaders();
                defaultHeaders.add("Accept", "application/vnd.elasticsearch+json;compatible-with=8");
                defaultHeaders.add("Content-Type", "application/vnd.elasticsearch+json;compatible-with=8");

                // Build the ClientConfiguration from scratch, combining properties-based config with our custom SSL context
                return ClientConfiguration.builder()
                        .connectedTo(uris)
                        .usingSsl(sslContext)
                        .withConnectTimeout(connectTimeout)
                        .withSocketTimeout(socketTimeout)
                        .withDefaultHeaders(defaultHeaders)
                        .withBasicAuth(username, password)
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error configuring Elasticsearch SSL client", e);
        }
    }
}