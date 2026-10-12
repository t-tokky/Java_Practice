package com.portfolio.qiita_summary_notifier.service;

import com.portfolio.qiita_summary_notifier.infrastructure.gemini.dto.GeminiResponseDto;

// geminiに要約してもらうためのインターフェース
public interface Summarizer {
    public GeminiResponseDto fetchSummaryOfArticle(Article article);
}
