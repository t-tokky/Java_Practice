package com.portfolio.qiita_summary_notifier.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// デモ画面で通知成功の可否を伝えるために追加した
@Data 
@AllArgsConstructor 
public class DemoResponse {
    private boolean success;  // 通知が成功したかどうか
    private String message;  // 表示メッセージ
}
