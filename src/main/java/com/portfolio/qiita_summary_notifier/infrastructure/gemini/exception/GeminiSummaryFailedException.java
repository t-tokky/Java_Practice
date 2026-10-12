package com.portfolio.qiita_summary_notifier.infrastructure.gemini.exception;

public class GeminiSummaryFailedException extends RuntimeException{

    public GeminiSummaryFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
