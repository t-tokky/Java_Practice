package com.portfolio.qiita_summary_notifier.security;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import com.portfolio.qiita_summary_notifier.entity.UserEntity;

public class CustomUserDetails extends User {
    
    private final Integer id;
    
    // コンストラクタを拡張して、idを保持する
    public CustomUserDetails(UserEntity user) {
        super(
            user.getUsername(), 
            user.getPasswordHash(), 
            AuthorityUtils.createAuthorityList("ROLE_USER")
        );
        this.id = user.getId();
    }
    
    public Integer getId() {
        return this.id;
    }
}
