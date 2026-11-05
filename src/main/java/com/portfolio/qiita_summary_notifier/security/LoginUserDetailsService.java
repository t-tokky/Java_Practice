package com.portfolio.qiita_summary_notifier.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.portfolio.qiita_summary_notifier.entity.UserEntity;
import com.portfolio.qiita_summary_notifier.repository.UserMapper;

@Service
public class LoginUserDetailsService implements UserDetailsService{

    private final UserMapper userMapper;

    LoginUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("ユーザーが見つかりませんでした: " + username);
        }

        return User.withUsername(username)
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .build();
    }
}
