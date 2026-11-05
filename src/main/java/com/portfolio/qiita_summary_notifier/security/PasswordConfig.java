package com.portfolio.qiita_summary_notifier.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    /**memo: エンコーダー
     * パスワードのハッシュ化(encode())や検証(matches())を行うのがこのメソッドが返すオブジェクト
     * 今回のPasswordEncoderオブジェクトは、ハッシュ値の先頭にアルゴリズム名を付与して保存する({bcrypt}$2a$10...)
     * これによって別アルゴリズムに移行しても接頭辞から識別が可能
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
