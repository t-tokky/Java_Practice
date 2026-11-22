package com.portfolio.qiita_summary_notifier.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;

@Mapper 
public interface NotificationSettingMapper {
    // 設定の新規登録
    void insert(NotificationSetting setting);

    // アクティブな設定の検索
    List<NotificationSetting> selectActiveSettings();

    // 設定の検索(1件のみ)
    NotificationSetting selectById(@Param("id") Integer id);

    // 設定の更新
    void update(NotificationSetting setting);

    // 設定の削除
    void delete(@Param("id") Integer id);

    // 通知送信をoffに切り替える
    void disableNotify(@Param("id") Integer id);
}
