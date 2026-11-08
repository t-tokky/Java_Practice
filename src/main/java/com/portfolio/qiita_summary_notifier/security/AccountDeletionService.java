package com.portfolio.qiita_summary_notifier.security;

import org.springframework.stereotype.Service;

import com.portfolio.qiita_summary_notifier.repository.UserMapper;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AccountDeletionService {

    private final UserMapper userMapper;

    public void deleteAccount(Integer id) {
        userMapper.delete(id);
    }
}
