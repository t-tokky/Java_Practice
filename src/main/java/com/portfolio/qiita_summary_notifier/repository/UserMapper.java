package com.portfolio.qiita_summary_notifier.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.portfolio.qiita_summary_notifier.entity.UserEntity;

@Mapper 
public interface UserMapper {
    // ユーザーを新規作成
    void insert(UserEntity user);

    // ログイン認証時に名前を使う
    UserEntity selectByUsername(@Param("username") String username);

    // 使用済みの名前かどうかを確認する
    boolean existsByUsername(@Param("username") String username);

    // ユーザー情報の削除
    void delete(@Param("id") Integer id);
}
