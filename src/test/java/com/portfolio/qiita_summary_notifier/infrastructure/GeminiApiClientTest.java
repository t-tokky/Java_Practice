package com.portfolio.qiita_summary_notifier.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

import org.junit.jupiter.api.Test;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import com.google.genai.types.HttpOptions;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.GeminiApiClient;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.exception.GeminiRateLimitException;
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
    void HTTP応答が429の後に成功した場合はリトライして要約DTOを返す() throws IOException {
        try (Fixture fixture = createFixture(attempt -> {
            if (attempt == 1) {
                return new ApiResponse(429, """
                        {"error":{"code":"RESOURCE_EXHAUSTED","message":"rate limited"}}
                        """);
            }
            return new ApiResponse(200, interactionJson());
        })) {
            GeminiResponseDto result =
                    fixture.client.fetchSummaryOfArticle(createArticle());

            assertEquals("Javaの例外処理", result.getTheme());
            // 1回目の429がカスタム例外に変換され、再試行されたことを確認
            assertEquals(2, fixture.requestCount());
        }
    }

    @Test
    void HTTP応答が400ではリトライせず空のDTOを返す() throws IOException {
        try (Fixture fixture = createFixture(
                attempt -> new ApiResponse(400, """
                        {"error":{"code":"INVALID_ARGUMENT","message":"bad request"}}
                        """))) {

            GeminiResponseDto result =
                    fixture.client.fetchSummaryOfArticle(createArticle());

            assertNull(result.getTheme());
            // ClientExceptionはリトライ対象外
            assertEquals(1, fixture.requestCount());
        }
    }

    @Test
    void HTTP応答が500では規定回数リトライした後に空のDTOを返す() throws IOException {
        try (Fixture fixture = createFixture(
                attempt -> new ApiResponse(500, """
                        {"error":{"code":"INTERNAL","message":"server error"}}
                        """))) {

            GeminiResponseDto result =
                    fixture.client.fetchSummaryOfArticle(createArticle());

            assertNull(result.getTheme());
            // maxRetries(2)なので初回1回＋再試行2回
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
            byte[] responseBody = response.body().getBytes();

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

        Client geminiClient = Client.builder()
                .apiKey("test-api-key")
                .httpOptions(HttpOptions.builder()
                        .baseUrl(baseUrl)
                        .apiVersion("v1beta")
                        .build())
                .build();

        GeminiApiClient apiClient = new GeminiApiClient(
                geminiClient,
                createTestRetryTemplate(),
                new ObjectMapper());

        return new Fixture(server, geminiClient, apiClient, requestCount);
    }

    private RetryTemplate createTestRetryTemplate() {
        return new RetryTemplate(
                RetryPolicy.builder()
                        .maxRetries(2)
                        .includes(
                                ServerException.class,
                                GenAiIOException.class,
                                GeminiRateLimitException.class)
                        .excludes(ClientException.class)
                        .delay(Duration.ZERO)
                        .build());
    }

    private Article createArticle() {
        Article article = new Article();
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