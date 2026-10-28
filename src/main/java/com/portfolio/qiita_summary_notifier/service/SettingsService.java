package com.portfolio.qiita_summary_notifier.service;

import java.util.List;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;

public interface SettingsService {
    // 新規登録 or 更新
    void saveSettings(NotificationSetting setting);
    // 設定一覧
    List<NotificationSetting> selectByUserId(Integer userId);
    // 設定を1件取得
    NotificationSetting selectById(Integer id);
    // 削除
    void delete(Integer id);

    // アクティブな設定一覧(全ユーザー対象)
    List<NotificationSetting> selectActiveSettings();
}
