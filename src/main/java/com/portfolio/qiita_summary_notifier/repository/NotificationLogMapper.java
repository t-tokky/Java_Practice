package com.portfolio.qiita_summary_notifier.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper 
public interface NotificationLogMapper {
    // 通知した結果を登録するメソッド
    void insertLog(
        @Param("settingId") Integer settingId, 
        @Param("articleId") String articleId
    );

    // 通知済みかを確認するためのメソッド
    boolean existsBySettingIdAndArticleId(
        @Param("settingId") Integer settingId,
        @Param("articleId") String articleId
    );
}
