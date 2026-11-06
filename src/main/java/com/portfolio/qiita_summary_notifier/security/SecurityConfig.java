package com.portfolio.qiita_summary_notifier.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity 
public class SecurityConfig {

    /**memo: SecurityConfigの設定
     * 許可が必要/不必要なURLを振り分けた
     * 基本的には一部のみアクセスを許して、後は許可を求めるようにする
     * これはあくまでログインしたユーザーすべてに設定しているので、
     * ここからユーザー個別にアクセスする(DBの範囲)設定はまた別で作る必要がある
     */

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
                    "/error"            // エラー画面
                ).permitAll()
                .anyRequest().authenticated()   // 上記以外のすべてのURLは許可したユーザーのみ
        )   // ログイン時の設定
            .formLogin(form -> form
                // .loginPage("/login") // 自作したログイン画面や処理をするときにコメントアウトをはずす
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
