package com.portfolio.qiita_summary_notifier.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.entity.UserEntity;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;
import com.portfolio.qiita_summary_notifier.repository.UserMapper;

import lombok.RequiredArgsConstructor;

// 新規ユーザーには必ず初期設定が必要なためエラーが起きたら差し戻せるようにした
@Transactional
@Service
@RequiredArgsConstructor 
public class SignupService {

    private final UserMapper userMapper;
    private final NotificationSettingMapper settingMapper;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(encodedPassword);

        userMapper.insert(user);
        initSetting(user);
    }

    // 初期設定をDBに追加
    private void initSetting(UserEntity user) {
        NotificationSetting setting = new NotificationSetting();
        setting.setUserId(user.getId());
        setting.setWebhookUrl("");

        settingMapper.insert(setting);
    }
}
