package com.portfolio.qiita_summary_notifier.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;
import com.portfolio.qiita_summary_notifier.service.SettingsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService{

    private final NotificationSettingMapper settingMapper;

    @Override 
    public void saveSettings(NotificationSetting setting) {
        if (setting.getId() != null) { // 既存の設定のとき
            // webhookUrlが入力されないとき、DBの値を取得
            if (setting.getWebhookUrl() == null || setting.getWebhookUrl().isEmpty()) {
                String currentWebhookUrl = settingMapper.selectById(setting.getId()).getWebhookUrl();
                setting.setWebhookUrl(currentWebhookUrl);
            }
            settingMapper.update(setting);
        } else {  // 新規設定の時
            // ここで自動採番(mybatis側)+採番されたidをsettingに代入している
            settingMapper.insert(setting);
        }
    };

    @Override 
    public List<NotificationSetting> selectByUserId(Integer userId) {
        return settingMapper.selectByUserId(userId);
    };
    
    @Override 
    public NotificationSetting selectById(Integer id) {
        return settingMapper.selectById(id);
    };
    
    @Override 
    public void delete(Integer id) {
        settingMapper.delete(id);
    };

    // 全ユーザーのアクティブな設定を取得
    @Override 
    public List<NotificationSetting> selectActiveSettings() {
        return settingMapper.selectActiveSettings();
    };
}
