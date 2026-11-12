package com.portfolio.qiita_summary_notifier.config;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
                    "/signup",          // アカウント作成画面
                    "/login",           // ログイン画面を明示的に これを入れないとパラメータ付きがはじかれた
                    "/qa",              // Q&A
                    "/terms",           // 利用規約
                    "/api/notifications/demo"   // デモ機能のAPI
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

    // =========================================================
    // DBテスト時にH2consoleにアクセスできるようにする設定
    // 本番環境では全部コメントアウトさせる
    @Bean
    @Order(1) // メインの設定より先に「H2行きか？」を判定させる
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
        
        http
            // このセキュリティ設定を、H2コンソールのURLに限定する
            .securityMatcher(PathRequest.toH2Console())
            // アクセスを全て許可
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // H2コンソール内ではCSRFを無効化（対象URLを限定しているのでdisable()で安全）
            .csrf(csrf -> csrf.disable())
            // iframe（画面分割）での表示を許可
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
    // =========================================================
}
