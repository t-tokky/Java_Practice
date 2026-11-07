package com.portfolio.qiita_summary_notifier.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity 
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http    // アクセス権限の範囲設定
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(       // 許可するURLの設定
                    "/",    // ランディングページ
                    "/demo",             // デモ画面
                    "/css/**",          // cssファイルのすべて
                    "/js/**",           // jsファイルのすべて
                    "/images/**",       // 画像のすべて
                    "/error",           // エラー画面
                    "/signup"
                ).permitAll()
                .anyRequest().authenticated()   // 上記以外のすべてのURLは許可したユーザーのみ
        )   // ログイン時の設定
            .formLogin(form -> form
                .loginPage("/login") // 自作したログイン画面や処理をするときにコメントアウトをはずす
                .defaultSuccessUrl("/")
                .permitAll()    // ログイン関係のすべてのURLへのアクセスを許可
        )   // ログアウト時の設定
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        return http.build();
    }
}
