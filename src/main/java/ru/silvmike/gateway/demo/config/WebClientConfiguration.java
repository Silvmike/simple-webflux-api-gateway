package ru.silvmike.gateway.demo.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {

        IOReactorConfig ioConfig =
            IOReactorConfig.custom()
                .setSoTimeout(3, TimeUnit.SECONDS)
                .setSoKeepAlive(true)
                .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2)
                .build();

        RequestConfig requestConfig =
            RequestConfig.custom()
                .setConnectionRequestTimeout(3, TimeUnit.SECONDS)
                .setResponseTimeout(2, TimeUnit.SECONDS)
                .build();

        CloseableHttpAsyncClient asyncClient = HttpAsyncClients.custom()
            .setConnectionManager(
                PoolingAsyncClientConnectionManagerBuilder.create()
                    .setDefaultTlsConfig(
                        TlsConfig.custom()
                            .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_1)
                            .build()
                    )
                    .setDefaultConnectionConfig(
                        ConnectionConfig.custom()
                            .setConnectTimeout(2, TimeUnit.SECONDS)
                            .setSocketTimeout(3, TimeUnit.SECONDS)
                            .setTimeToLive(5, TimeUnit.MINUTES)
                            .build()
                    )
                    .setMaxConnPerRoute(10)
                    .setMaxConnTotal(20)
                    .build()
            )
            .setDefaultRequestConfig(requestConfig)
            .setIOReactorConfig(ioConfig)
            .disableRedirectHandling()
            .disableAuthCaching()
            .disableCookieManagement()
            .build();

        return WebClient.builder()
            .clientConnector(
                new HttpComponentsClientHttpConnector(asyncClient)
            );
    }

}
