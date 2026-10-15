package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.qiita_summary_notifier.controller.dto.DemoResponse;
import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.service.QiitaNotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping ("/api/notifications")
@RequiredArgsConstructor 
public class NotificationApiController {

    private final QiitaNotificationService qiitaNotificationService;

    /**memo: デモボタンの処理
     * ボタンを押されたら入力された値をもとに処理を実行
     * 送信できたかどうかを返す
     */
    @PostMapping("/demo")
    public DemoResponse demoNotification(@RequestBody NotificationSetting setting, boolean isDemo) {
        boolean success = qiitaNotificationService.deliverArticles(setting, isDemo);
        if (success) {
            return new DemoResponse(success, "送信が成功しました");
        } else {
            // 今後メッセージは変える(入力ミス、サーバーエラーなどで切り替え)
            return new DemoResponse(success, "送信に失敗しました");
        }

    }
}