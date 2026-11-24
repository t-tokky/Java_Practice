package com.portfolio.qiita_summary_notifier.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Configuration
public class RetryConfig {
    // Qiita API用: 復旧の可能性があるもののみリトライさせる
    @Bean
    @Qualifier ("qiitaRetryTemplate")
    public RetryTemplate qiitaRetryTemplate() {
        return new RetryTemplate(
            RetryPolicy.builder()
                    .maxRetries(2)
                    .includes(ResourceAccessException.class, HttpServerErrorException.class)
                    .excludes(HttpClientErrorException.class)
                    .delay(Duration.ofSeconds(2))
                    .multiplier(2.0)
                    .maxDelay(Duration.ofSeconds(10))
                    .build()
        );
    }

    // Discord API用: APIごとの特性に合わせて個別調整をするため、独立して定義する
    @Bean 
    @Qualifier ("discordRetryTemplate")
    public RetryTemplate discordRetryTemplate() {
        return new RetryTemplate(
            RetryPolicy.builder()
                    .maxRetries(2)
                    .includes(ResourceAccessException.class, HttpServerErrorException.class)
                    .excludes(HttpClientErrorException.class)
                    .delay(Duration.ofSeconds(2))
                    .multiplier(2.0)
                    .maxDelay(Duration.ofSeconds(10))
                    .build()
        );
    }

    // FIXME: DBアクセス用 当初クラウドDBを使う予定で設定していたが、内部DBに切り替わったため要修正
    @Bean 
    @Qualifier ("dbRetryTemplate")
    public RetryTemplate dbRetryTemplate() {
        return new RetryTemplate(
            RetryPolicy.builder()
                    .maxRetries(2)
                    .includes(TransientDataAccessException.class)
                    .excludes(NonTransientDataAccessException.class)
                    .delay(Duration.ofSeconds(2))
                    .multiplier(2.0)
                    .maxDelay(Duration.ofSeconds(10))
                    .build()
        );
    }

}
