package com.portfolio.qiita_summary_notifier.config;

import java.time.Duration;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;

import okhttp3.OkHttpClient;

@Configuration
public class GeminiClientConfig {

    // ClientクラスはGemini APIライブラリのオブジェクトなので、Gemini APIのドキュメントを参照すること
    @Bean
    Client geminiClient(@Value("${gemini.api.token}") String apiToken) {

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10)) // 接続確立まで
                .readTimeout(Duration.ofSeconds(60))    // データ受信中の待ち時間
                .callTimeout(Duration.ofSeconds(90))    // リクエスト全体
                .build();

        HttpRetryOptions retryOptions = HttpRetryOptions.builder()
                .attempts(3) // 初回を含めて最大3回
                .httpStatusCodes(
                    IntStream.concat(
                        IntStream.of(429),  // 429 too many request だけリトライさせる
                        IntStream.rangeClosed(500, 599))
                    .boxed()
                    .toList())
                .build();

        return Client.builder()
                .apiKey(apiToken)
                .httpOptions(HttpOptions.builder()
                        .retryOptions(retryOptions)
                        .build())
                .clientOptions(ClientOptions.builder()
                        .customHttpClient(httpClient)
                        .build())
                .build();
    }
}
