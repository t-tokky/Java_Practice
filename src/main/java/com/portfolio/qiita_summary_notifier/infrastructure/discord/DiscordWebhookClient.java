package com.portfolio.qiita_summary_notifier.infrastructure.discord;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portfolio.qiita_summary_notifier.infrastructure.discord.dto.DiscordWebhookRequestDto;
import com.portfolio.qiita_summary_notifier.service.ArticleWithSummary;
import com.portfolio.qiita_summary_notifier.service.NotificationSender;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component
public class DiscordWebhookClient implements NotificationSender {

    private final RestClient discordRestClient;
    private final RetryTemplate retryTemplate;

    public DiscordWebhookClient(
        @Qualifier("discordRestClient") RestClient discordRestClient, 
        @Qualifier ("discordRetryTemplate") RetryTemplate retryTemplate
    ) {
        this.discordRestClient = discordRestClient;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public boolean notifyNewArticles(String webhookUrl, List<ArticleWithSummary> unformattedDataList) {

        /**memo: 送信の一連の流れ
         * まずDtoを使って受け取った生のコンテンツを整形する
         * それをRestClientのbodyに渡して送信する
         */
        DiscordWebhookRequestDto contents = createEmbed(unformattedDataList);
        // ここでcontents(というかunformattedDataList)が空のオブジェクトなら
        // "Qiita通知Bot"という名前で、"記事情報を取得できませんでした"というメッセージを送りたい
        // そのためにここでオブジェクトを手作りしてしまってもいいのか
        // embed.addFieldで以上のテキストだけ作って送ってもいいのかも

        log.info("送信リクエストデータ: {}", contents);
        try {
            retryTemplate.invoke(() -> {
                discordRestClient.post()
                    .uri(webhookUrl)
                    .body(contents)
                    .retrieve()
                    // 戻ってくる要素がないときにこのメソッドが必要
                    .toBodilessEntity();
            });

            // 空のリストを受け取っても通知は行うが、戻り値はfalse(記事は送れなかったため)
            return !unformattedDataList.isEmpty();
        } catch (RestClientException e) {
            log.error("Discordへの送信時に発生: {}", e);
            return false;
        }
    }

    private DiscordWebhookRequestDto createEmbed(List<ArticleWithSummary> unformattedDataList) {
        /**memo: Dtoを使って記事データと要約情報を組み立てる
         * 最終的にDtoを返して、それをRestClientのbodyに渡せばいいはず
         */
        DiscordWebhookRequestDto contents = new DiscordWebhookRequestDto();

        for (ArticleWithSummary article : unformattedDataList) {
            // インナークラスをインスタンス化
            DiscordWebhookRequestDto.Embed embed = new DiscordWebhookRequestDto.Embed();

            embed.setTitle(article.getTitle());
            embed.setUrl(article.getUrl());
            embed.setColor(5620992);

            // 投稿者名をauthorとして作成(文字列フォーマットで作成、逆にわかりずらい？)
            DiscordWebhookRequestDto.Author author = new DiscordWebhookRequestDto.Author
                (String.format("%s(%s)", article.getUsername(), article.getUserId()));
            embed.setAuthor(author);

            embed.addField("テーマ", article.getTheme(), false);
            embed.addField("ポイント", article.getKeyTakeaways(), false);
            embed.addField("読者層", article.getTargetAudience(), false);

            DiscordWebhookRequestDto.Footer footer = 
                new DiscordWebhookRequestDto.Footer(String.join(", ", article.getTags()));
            embed.setFooter(footer);

            contents.addEmbed(embed);
        }

        // 記事(要約)が取得できなかったときに通知したい内容
        if (unformattedDataList.isEmpty()) {
            DiscordWebhookRequestDto.Embed embed = new DiscordWebhookRequestDto.Embed();
            embed.addField("お知らせ", "記事を取得できませんでした。\n次回の通知をお待ちください。", false);
            contents.addEmbed(embed);
        }

        return contents;
    }
}
