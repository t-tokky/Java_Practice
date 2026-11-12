# H2DBのメモ

テスト用にH2を入れたのでやったことだけでもメモ

### 作業内容

#### 依存関係を追加

- `runtimeOnly 'com.h2database:h2'`
- `implementation 'org.springframework.boot:spring-boot-h2console'`
- 動かすだけならruntimeOnlyだけ入れたらいい
- コンソール画面を開くには新しいバージョンのspringbootだと依存関係が必要

#### `application-local.properties`にH2の設定を追加
これは公開してしまってもいいらしい(テスト時のメモリの上でしか動かないため)
これをコピペして貼り付けておく
```application-local.properties
# H2 (インメモリ) 設定
# H2はpostgreSQLではないが互換性があるのでモード指定をする
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL  
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true  GUIでのコンソール画面を表示
spring.h2.console.path=/h2-console

# 起動時に schema.sql / data.sql を自動実行する設定
spring.sql.init.mode=always
```

#### H2コンソール

- JDBC URLに登録した値(`jdbc:h2:mem:testdb;MODE=PostgreSQL`)を入れて接続すれば見れる
- このURLはキャッシュされてブラウザに残るが、消える場合は設定名の保存を押すと保存できる
- コンソールは基本的にテーブルをクリックして実行ボタンを押せば内容が見れる
- 詳しい方法は必要になってからでOK