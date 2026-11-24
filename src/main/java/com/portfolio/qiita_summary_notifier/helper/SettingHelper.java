package com.portfolio.qiita_summary_notifier.helper;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.form.SettingForm;

public class SettingHelper {

    public static NotificationSetting convertSetting(SettingForm form) {
        NotificationSetting setting = new NotificationSetting();
        setting.setId(form.getId());
        setting.setIncludeTags(form.getIncludeTags());
        setting.setExcludeTags(form.getExcludeTags());
        setting.setWebhookName(form.getWebhookName());
        setting.setWebhookUrl(form.getWebhookUrl());
        setting.setIsActive(form.getIsActive());

        return setting;
    }

    public static SettingForm convertForm(NotificationSetting setting) {
        SettingForm form = new SettingForm();
        form.setId(setting.getId());
        form.setIncludeTags(setting.getIncludeTags());
        form.setExcludeTags(setting.getExcludeTags());
        form.setWebhookName(setting.getWebhookName());
        form.setWebhookUrl(setting.getWebhookUrl());
        form.setIsActive(setting.getIsActive());

        return form;
    }
}
