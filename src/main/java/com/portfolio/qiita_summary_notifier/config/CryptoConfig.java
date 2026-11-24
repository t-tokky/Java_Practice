package com.portfolio.qiita_summary_notifier.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesGcmBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import com.portfolio.qiita_summary_notifier.security.MyHexEncodingTextEncryptor;

@Configuration 
public class CryptoConfig {

    @Value("${app.crypto.secret-key}")
    private String secretKey;

    @Value("${app.crypto.salt}")
    private String salt;

    // AES-GCM方式(暗号化+改ざんチェック)のTextEncryptor(暗号化の道具)を作る
    @Bean
    public TextEncryptor textEncryptor() {
        
        BytesEncryptor bytesEncryptor = AesGcmBytesEncryptor.withPassword(secretKey, salt).build();
        return new MyHexEncodingTextEncryptor(bytesEncryptor);
    }
}
