package com.portfolio.qiita_summary_notifier.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.portfolio.qiita_summary_notifier.entity.UserEntity;
import com.portfolio.qiita_summary_notifier.repository.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class SignupService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(encodedPassword);

        userMapper.insert(user);
        
    }
}
