package com.portfolio.qiita_summary_notifier.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.exception.GeminiSummaryFailedException;
import com.portfolio.qiita_summary_notifier.repository.NotificationLogMapper;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service
public class QiitaNotificationService {

    private final ArticleProvider articleProvider;
    private final NotificationSender notificationSender;
    private final Summarizer summarizer;

    private final NotificationSettingMapper notificationSettingMapper;
    private final NotificationLogMapper notificationLogMapper;

    private final RetryTemplate dbRetryTemplate;

    // RetryTemplate型のBeanが複数あり、引数に@Qualifierを明示する必要があるためコンストラクタを手書きにしている
    public QiitaNotificationService(
        ArticleProvider articleProvider,
        NotificationSender notificationSender,
        Summarizer summarizer,
        NotificationSettingMapper notificationSettingMapper,
        NotificationLogMapper notificationLogMapper,
        @Qualifier("dbRetryTemplate") RetryTemplate dbRetryTemplate
    ) {
        this.articleProvider = articleProvider;
        this.notificationSender = notificationSender;
        this.summarizer = summarizer;
        this.notificationSettingMapper = notificationSettingMapper;
        this.notificationLogMapper = notificationLogMapper;
        this.dbRetryTemplate = dbRetryTemplate;
    }

    // 有効な設定を全件取得してループで回す
    // @Scheduled (cron = "0 * * * * *", zone = "Asia/Tokyo") // 毎分実行、テスト用
    @Scheduled (cron = "0 30 7 * * *", zone = "Asia/Tokyo") // AM7:30実行 本番用
    public void execute() {

        log.info("バッチ処理を開始");
        
        try {
            dbRetryTemplate.invoke(() -> {
                List<NotificationSetting> settingsList = 
                    notificationSettingMapper.selectActiveSettings();
                for (NotificationSetting setting : settingsList) {
                    deliverArticles(setting);
                }
            });
        } catch(DataAccessException e) {
            log.error("定期処理が実行できませんでした。\nDBにアクセスできません。\n{}", e);
            return;
        }
        log.info("バッチ処理が終了");
    }

    // デモ実行との区別をするためにオーバーロードしている
    public void deliverArticles(NotificationSetting setting) {
        deliverArticles(setting, false);
    }

    // デモ機能用にオーバーロードしている
    public boolean deliverArticles(NotificationSetting setting, boolean isDemo) {

        String includeTags = setting.getIncludeTags();
        String excludeTags = setting.getExcludeTags();
        List<Article> articles = articleProvider.fetchArticles(includeTags, excludeTags);
        
        // まだ送信していない記事のみに振り分ける
        Integer settingId = setting.getId();
        List<Article> latestArticles = articles.stream()
                .filter(article -> !notificationLogMapper.existsBySettingIdAndArticleId(settingId, article.getId()))
                .toList();

        // 要約結果と記事データを組み合わせてDiscordへ渡すためのオブジェクトにする
        List<ArticleWithSummary> unformattedList = new ArrayList<>();
        for (Article article : latestArticles) {
            try {
                ArticleWithSummary unformattedData = 
                    new ArticleWithSummary(article, summarizer.fetchSummaryOfArticle(article));
                unformattedList.add(unformattedData);
            } catch (GeminiSummaryFailedException e) {
                log.error("GeminiApiエラー: {}", e);
            }
        }

        // Discordへ通知
        boolean isCompleteNotify = false;
        try {
            isCompleteNotify = notificationSender.notifyNewArticles(
                setting.getWebhookUrl(), unformattedList
            );

        // URLが存在しない場合、通知を送らない設定へ切り替える
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Webhookの404エラー：ユーザーID{}の通知設定をOFFに切り替え", settingId);

            notificationSettingMapper.disableNotify(settingId);
        }

        // 通知履歴をDBへ保存
        if (isCompleteNotify && !isDemo) {
            try {
                dbRetryTemplate.invoke(() -> {
                    for (Article sentArticle : latestArticles) {
                        notificationLogMapper.insertLog(settingId, sentArticle.getId());
                    }
                });
            } catch (DataAccessException e) {
                log.error("通知履歴の保存でエラー発生: {}", e);
            }

        }

        // デモ機能のための変数
        // 定期実行時はこの値は使わない
        return isCompleteNotify;
    }
}
