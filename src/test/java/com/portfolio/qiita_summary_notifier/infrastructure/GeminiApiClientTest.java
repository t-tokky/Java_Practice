package com.portfolio.qiita_summary_notifier.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.errors.ServerException;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.GeminiApiClient;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.exception.GeminiSummaryFailedException;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.ObjectMapper;

class GeminiApiClientTest {

    private static final String SUMMARY_JSON = """
            {
              "theme": "Javaの例外処理",
              "key_takeaways": "例外処理の基本を学べます",
              "target_audience": "例外処理を学び始めた開発者"
            }
            """;

    @Test
    void API成功時は要約DTOを返す() throws IOException {
        try (Fixture fixture = createFixture(
                attempt -> new ApiResponse(200, interactionJson()))) {

            GeminiResponseDto result =
                    fixture.client.fetchSummaryOfArticle(createArticle());

            assertEquals("Javaの例外処理", result.getTheme());
            assertEquals("例外処理の基本を学べます", result.getKeyTakeaways());
            assertEquals("例外処理を学び始めた開発者", result.getTargetAudience());
            assertEquals(1, fixture.requestCount());
        }
    }

    @Test
    void HTTP応答が429の後に成功した場合はSDKがリトライして要約DTOを返す()
            throws IOException {
        try (Fixture fixture = createFixture(attempt -> {
            if (attempt == 1) {
                return new ApiResponse(429, errorJson());
            }
            return new ApiResponse(200, interactionJson());
        })) {
            GeminiResponseDto result =
                    fixture.client.fetchSummaryOfArticle(createArticle());

            assertEquals("Javaの例外処理", result.getTheme());
            assertEquals(2, fixture.requestCount());
        }
    }

    @Test
    void HTTP応答が400ではリトライせずGeminiSummaryFailedExceptionを投げる()
            throws IOException {
        try (Fixture fixture = createFixture(
                attempt -> new ApiResponse(400, errorJson()))) {

            GeminiSummaryFailedException exception =
                    assertThrows(
                            GeminiSummaryFailedException.class,
                            () -> fixture.client.fetchSummaryOfArticle(
                                    createArticle()));

            assertTrue(exception.getMessage().contains("article-1"));
            assertInstanceOf(ClientException.class, exception.getCause());
            assertEquals(1, fixture.requestCount());
        }
    }

    @Test
    void HTTP応答が500が続くと最大3回試行した後にGeminiSummaryFailedExceptionを投げる()
            throws IOException {
        try (Fixture fixture = createFixture(
                attempt -> new ApiResponse(500, errorJson()))) {

            GeminiSummaryFailedException exception =
                    assertThrows(
                            GeminiSummaryFailedException.class,
                            () -> fixture.client.fetchSummaryOfArticle(
                                    createArticle()));

            assertTrue(exception.getMessage().contains("article-1"));
            assertInstanceOf(ServerException.class, exception.getCause());
            assertEquals(3, fixture.requestCount());
        }
    }

    @Test
    void HTTP応答が429が続くと最大3回試行した後にGeminiSummaryFailedExceptionを投げる()
            throws IOException {
        try (Fixture fixture = createFixture(
                attempt -> new ApiResponse(429, errorJson()))) {

            GeminiSummaryFailedException exception =
                    assertThrows(
                            GeminiSummaryFailedException.class,
                            () -> fixture.client.fetchSummaryOfArticle(
                                    createArticle()));

            assertTrue(exception.getMessage().contains("article-1"));
            assertInstanceOf(ClientException.class, exception.getCause());
            assertEquals(3, fixture.requestCount());
        }
    }

    private Fixture createFixture(IntFunction<ApiResponse> responder)
            throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress("localhost", 0), 0);
        AtomicInteger requestCount = new AtomicInteger();

        server.createContext("/", exchange -> {
            int attempt = requestCount.incrementAndGet();
            ApiResponse response = responder.apply(attempt);
            byte[] responseBody =
                    response.body().getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json");
            exchange.sendResponseHeaders(
                    response.statusCode(), responseBody.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(responseBody);
            }
        });

        server.start();

        String baseUrl = "http://localhost:"
                + server.getAddress().getPort();

        HttpRetryOptions retryOptions = HttpRetryOptions.builder()
                .attempts(3) // 初回を含めて最大3回
                .httpStatusCodes(
                        IntStream.concat(
                                IntStream.of(429),
                                IntStream.rangeClosed(500, 599))
                                .boxed()
                                .toList())
                .initialDelay(0.0)
                .maxDelay(0.0)
                .jitter(0.0)
                .build();

        Client geminiClient = Client.builder()
                .apiKey("test-api-key")
                .httpOptions(HttpOptions.builder()
                        .baseUrl(baseUrl)
                        .apiVersion("v1beta")
                        .retryOptions(retryOptions)
                        .build())
                .build();

        GeminiApiClient apiClient =
                new GeminiApiClient(geminiClient, new ObjectMapper());

        return new Fixture(server, geminiClient, apiClient, requestCount);
    }

    private Article createArticle() {
        Article article = new Article();
        article.setId("article-1");
        article.setTitle("テスト記事");
        article.setBody("テスト本文");
        return article;
    }

    private String interactionJson() {
        String escapedSummary = SUMMARY_JSON
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        return """
                {
                  "id": "test-interaction",
                  "status": "completed",
                  "outputs": [
                    {
                      "type": "text",
                      "text": "%s"
                    }
                  ]
                }
                """.formatted(escapedSummary);
    }

    private String errorJson() {
        return """
                {
                  "error": {
                    "code": "TEST_ERROR",
                    "message": "test error"
                  }
                }
                """;
    }

    private record ApiResponse(int statusCode, String body) {
    }

    private static class Fixture implements AutoCloseable {
        private final HttpServer server;
        private final Client geminiClient;
        private final GeminiApiClient client;
        private final AtomicInteger requestCount;

        private Fixture(
                HttpServer server,
                Client geminiClient,
                GeminiApiClient client,
                AtomicInteger requestCount) {
            this.server = server;
            this.geminiClient = geminiClient;
            this.client = client;
            this.requestCount = requestCount;
        }

        private int requestCount() {
            return requestCount.get();
        }

        @Override
        public void close() {
            geminiClient.close();
            server.stop(0);
        }
    }
}