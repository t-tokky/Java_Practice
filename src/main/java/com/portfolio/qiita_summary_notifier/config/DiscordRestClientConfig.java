package com.portfolio.qiita_summary_notifier.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration 
public class DiscordRestClientConfig {

    @Bean 
    @Qualifier ("discordRestClient")
    RestClient discordRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
        
}
