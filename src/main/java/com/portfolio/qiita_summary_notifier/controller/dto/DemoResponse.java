package com.portfolio.qiita_summary_notifier.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class DemoResponse {
    private boolean success;  // 通知が成功したかどうか
    private String message;  // 画面に表示するための文字列
}
