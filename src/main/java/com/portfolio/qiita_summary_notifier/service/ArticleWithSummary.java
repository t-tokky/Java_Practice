package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;

import lombok.Data;

@Data
public class ArticleWithSummary {
    private String title;
    private List<String> tags;
    private String username;
    private String userId;
    private String theme;
    private String keyTakeaways;
    private String targetAudience;
    private String url;

    // 通知内容に記事のタグ情報を載せたかったので、タグをリストに変換している
    public ArticleWithSummary(Article article,  GeminiResponseDto summary) {
        this.title = article.getTitle();
        this.tags = article.getTags().stream().map(tag -> tag.getName()).toList();
        this.username = article.getUser().getName();
        this.userId = article.getUser().getId();
        this.theme = summary.getTheme();
        this.keyTakeaways = summary.getKeyTakeaways();
        this.targetAudience = summary.getTargetAudience();
        this.url = article.getUrl();
    }
}
