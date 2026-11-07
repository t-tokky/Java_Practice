# Spring Securityのメモ

## ユーザー認証の流れ

1. `UsernamePasswordAuthenticationFilter`  
ブラウザから送られてきたID(今回はusername)とパスワードを受け取るためのfilter  
Spring Securityによって自動で作成される

2. `UserDetailsService`  
受け取ったIDをもとにDBを検索し、IDに紐づいたパスワードを取得  
そのパスワードをUserDetailsというSecurityが読めるフォーマットにする

3. `PasswordEncoder`  
取得したパスワードと、ユーザーから送られてきたパスワードを比較して照合する  

### 補足

- `UserDetailsService` 
    - このインターフェースを実装したクラスを作る
    - 実装クラスは必ず`UsernameNotFoundException`を出すが、この例外はSpring Securityが例外処理を作ってくれているので、入れなかったときのメッセージくらいでいい？
    - `User`という認証用のフォーマットになっているオブジェクトに入れないとSpring Securityは認証ができない  
    `return User.withUsername(username) ...`この部分
- `PasswordEncoder`
    - 入力されたパスワードをハッシュ化して合致するかを調べるためのオブジェクト
    - ハッシュ化するアルゴリズムはいろいろある

## SecurityConfig

どのユーザーがどの範囲までアクセスできるかという、**許可**を設定するためのクラス  
`SecurityFilterChain`クラスのオブジェクトに設定情報を入れてbuildする  

### 基本設定

引数の`http`に対してメソッドチェーンで設定を追加する  
大まかに3つのルール設定があり、
- アクセスを許可する範囲 -> `authorizeHttpRequests()`
- ログイン時の設定 -> `formLogin()`
- ログアウト時の設定 -> `logout()`  

このようなブロックで設定する内容が分かれている  
設定情報は先に書かれたものが優先されるので、誰でも見ていい部分を先に設定しておいて、残りのページは認証を求めるようにするといい

```SecurityConfig.java
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
```

#### authorizeHttpRequests() 

どのURLに誰がアクセスできるかを決める  
**許可するもの**
- `/css`や`/js`、`/images`のようなファイル  
- ランディングページ(サイトのトップ)やエラー画面(`/error`)  

**許可しないもの**
- ログインユーザー個別のページ(`/setting/detail`など)

#### formLogin(), logout()

ログイン(ログアウト)ページの指定や`.permitAll()`による関連処理の一括許可など  
デフォルト設定のため、省略されている記述もある(下記を参照)
```java
.logout(logout -> logout
    .logoutUrl("/logout")                   // ログアウト処理を行うURL
    .logoutSuccessUrl("/login?logout")     // ログアウト完了後の遷移先
    .invalidateHttpSession(true)            // HTTPセッションを無効化（デフォルトで有効）
    .deleteCookies("JSESSIONID")            // JSESSIONIDクッキーを削除
    .permitAll()                            // ログアウト処理へのアクセスを許可
);
```

## ログイン後の流れ

idとpasswordを入力した後の動きをまとめておく  

1. **ログイン成功時**  
**サーバー側**には`Session/HttpSession`という、現在ログインしているユーザーの名簿データが作られる  
**ブラウザ**には`JSESSIONID`というCookieが渡される
2. **画面遷移時**  
ブラウザが、保持している`JSESSIONID`をもってサーバーへアクセスのリクエストを送る
3. **ビジネスロジック前の準備**  
Spring SecurityがSessionからユーザー情報を取り出し、`SecurityContext`というオブジェクトにセットする
4. **目的のビジネスロジックを実行**  
Controllerが引数に`@AuthenticationPrincipal`をつけることで`SecurityContext`からユーザー情報を取り出し、ビジネスロジックで使う

本来は`AuthenticationManager`や`SecurityContextHolder`といったものが出てくるがいったん割愛  

### メモ
