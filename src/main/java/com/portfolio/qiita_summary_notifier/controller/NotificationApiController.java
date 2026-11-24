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

    @PostMapping("/demo")
    public DemoResponse demoNotification(@RequestBody NotificationSetting setting) {
        boolean success = qiitaNotificationService.deliverArticles(setting, true);
        if (success) {
            return new DemoResponse(success, "送信が成功しました");
        } else {
            return new DemoResponse(success, "送信に失敗しました");
        }
    }
}