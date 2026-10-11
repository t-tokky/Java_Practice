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

    @Bean
    Client geminiClient(@Value("${gemini.api.token}") String apiToken) {
        /**memo: geminiAPIのClientオブジェクトを作成
         * OkHttpClientでタイムアウト設定をしている
         * geminiAPIライブラリは内部でOkHttpClientという別のライブラリを採用している(?)のでこちらの記述方法
         * callTimeout: 全体のタイムアウト時間
         * 
         * ClientはgeminiAPIライブラリのオブジェクト
         * 構造化スキーマやプロンプトの設定からHTTPリクエストまで全部やるオブジェクト
         * new Client:      環境変数にapiKeyを置いておけば自動で読み込むので.apiKey()が必要ない
         *                  今回はlocalから読み込むので.apiKeyが必要だった
         * builder, build:  ビルダーパターンのひとつ
         *                  最近のライブラリだとよく使われるらしい(RestClientもそうだった)
         * apiKey:          apiKeyを設定  
         * clientOptions:   引数でOkHttpClientオブジェクトをhttpClientオブジェクトとして作り直している(?)
         */
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10)) // 接続確立まで
                .readTimeout(Duration.ofSeconds(60))    // データ受信中の待ち時間
                .callTimeout(Duration.ofSeconds(90))    // リクエスト全体
                .build();

        HttpRetryOptions retryOptions = HttpRetryOptions.builder()
                .attempts(3) // 初回を含めて最大3回
                .httpStatusCodes(
                        IntStream.concat(
                                IntStream.of(429),
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
