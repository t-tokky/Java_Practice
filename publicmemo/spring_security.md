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

## 補足

- `UserDetailsService` 
    - このインターフェースを実装したクラスを作る
    - 実装クラスは必ず`UsernameNotFoundException`を出すが、この例外はSpring Securityが例外処理を作ってくれているので、入れなかったときのメッセージくらいでいい？
    - `User`という認証用のフォーマットになっているオブジェクトに入れないとSpring Securityは認証ができない  
    `return User.withUsername(username) ...`この部分
- `PasswordEncoder`
    - 入力されたパスワードをハッシュ化して合致するかを調べるためのオブジェクト
    - ハッシュ化するアルゴリズムはいろいろある