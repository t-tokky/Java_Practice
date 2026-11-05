package com.portfolio.qiita_summary_notifier.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.portfolio.qiita_summary_notifier.entity.UserEntity;

@Mapper 
public interface UserMapper {

    void insert(UserEntity user);

    UserEntity selectById(@Param("id") Integer id);

    UserEntity selectByUsername(@Param("username") String username);

    void update(UserEntity user);

    void delete(@Param("id") Integer id);
}
