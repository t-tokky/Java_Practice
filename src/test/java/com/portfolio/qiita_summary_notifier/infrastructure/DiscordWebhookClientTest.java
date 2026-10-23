package com.portfolio.qiita_summary_notifier.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.portfolio.qiita_summary_notifier.infrastructure.discord.DiscordWebhookClient;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;
import com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto.QiitaArticleDto;
import com.portfolio.qiita_summary_notifier.service.Article;
import com.portfolio.qiita_summary_notifier.service.ArticleWithSummary;

class DiscordWebhookClientTest {

    private static final String WEBHOOK_URL =
            "https://discord.test/api/webhooks/test";

    @Test
    void 記事がある場合は通知してtrueを返す() {
        Fixture fixture = createFixture();

        fixture.server.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.embeds[0].title").value("テスト記事"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        boolean result = fixture.client.notifyNewArticles(
                WEBHOOK_URL, List.of(createArticleWithSummary()));

        assertTrue(result);
        fixture.server.verify();
    }

    @Test
    void 空リストの場合はお知らせを送信してfalseを返す() {
        Fixture fixture = createFixture();

        fixture.server.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.embeds[0].fields[0].name")
                        .value("お知らせ"))
                .andExpect(jsonPath("$.embeds[0].fields[0].value")
                        .value("記事を取得できませんでした。\n次回の通知をお待ちください。"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        boolean result = fixture.client.notifyNewArticles(
                WEBHOOK_URL, List.of());

        assertFalse(result);
        fixture.server.verify();
    }

    @Test
    void 通信エラーの場合はfalseを返す() {
        Fixture fixture = createFixture();

        fixture.server.expect(times(3), requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
                
        boolean result = fixture.client.notifyNewArticles(
                WEBHOOK_URL, List.of(createArticleWithSummary()));

        assertFalse(result);
        fixture.server.verify();
    }

    @Test
    void HTTP応答が500エラーの後にリトライして成功した場合はtrueを返す() {
        Fixture fixture = createFixture();

        // 1回目はサーバーエラー
        fixture.server.expect(once(), requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // リトライした2回目は成功
        fixture.server.expect(once(), requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        boolean result = fixture.client.notifyNewArticles(
                WEBHOOK_URL, List.of(createArticleWithSummary()));

        assertTrue(result);
        fixture.server.verify();
    }

    private Fixture createFixture() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        RetryTemplate retryTemplate = new RetryTemplate(
                RetryPolicy.builder()
                        .maxRetries(2)
                        .includes(
                                ResourceAccessException.class,
                                HttpServerErrorException.class)
                        .excludes(HttpClientErrorException.class)
                        .delay(Duration.ZERO)
                        .build());

        DiscordWebhookClient client =
                new DiscordWebhookClient(builder.build(), retryTemplate);

        return new Fixture(client, server);
    }

    private ArticleWithSummary createArticleWithSummary() {
        QiitaArticleDto.User user = new QiitaArticleDto.User();
        user.setId("user-1");
        user.setName("テストユーザー");

        QiitaArticleDto.Tag tag = new QiitaArticleDto.Tag();
        tag.setName("Java");

        Article article = new Article(
                "テスト記事",
                "https://qiita.com/example",
                "本文",
                "article-1",
                user,
                List.of(tag));

        GeminiResponseDto summary = new GeminiResponseDto();
        summary.setTheme("テーマ");
        summary.setKeyTakeaways("ポイント");
        summary.setTargetAudience("読者層");

        return new ArticleWithSummary(article, summary);
    }

    private record Fixture(
            DiscordWebhookClient client,
            MockRestServiceServer server) {
    }


//         @Test
//         void 画像用ダミー() {
//         String webhookUrl = "";

//         var payload = new DiscordWebhookRequestDto();
//         var embed = new DiscordWebhookRequestDto.Embed();

//         embed.setTitle("【デモ】SpringBootで作る初めてのWebアプリ");
//         embed.setUrl("https://example.com/demo-article");
//         embed.setColor(5620992);
//         embed.setAuthor(
//                 new DiscordWebhookRequestDto.Author("demo_user (demo_user)")
//         );
//         embed.addField(
//                 "テーマ",
//                 "Spring Bootを用いたWebアプリケーション開発の基本構造と、最初の画面を表示するまでの手順についての記事です。",
//                 false
//         );
//         embed.addField(
//                 "ポイント",
//                 "MVCアーキテクチャの概念から、Thymeleafを用いた動的なHTMLの生成、Controllerでのルーティング設定まで、サンプルコードを交えてWeb開発の基礎的な流れが学べます。",
//                 false
//         );
//         embed.addField(
//                 "読者層",
//                 "Javaの基本構文を学習し終え、これからフレームワークを活用して本格的なWebアプリケーション開発に挑戦したいと考えているプログラミング初学者向け。",
//                 false
//         );
//         embed.setFooter(
//                 new DiscordWebhookRequestDto.Footer("Java, SpringBoot, Thymeleaf, 初心者, Web開発")
//         );

//         payload.addEmbed(embed);

//         RestClient.create()
//                 .post()
//                 .uri(webhookUrl)
//                 .body(payload)
//                 .retrieve()
//                 .toBodilessEntity();
//     }
}