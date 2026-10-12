package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

// Discordに通知を送るためのインターフェース
public interface NotificationSender {
    public boolean notifyNewArticles(String webhookUrl, List<ArticleWithSummary> unformattedDataList);
}
