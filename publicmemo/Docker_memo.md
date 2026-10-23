# Dockerを使ったデプロイについてのメモ

Dockerを使うことになったので簡単なメモ  
また作業が落ち着いたら詳しく調べる

## Dockerfileの内容
```Dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/qiita-summary-notifier-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```
- FROM: どんな環境が必要かを指定
- WORKDIR: 作業環境の指定、cdコマンドと同じ
- COPY: 手元のコード(今回はgithubのソースコード)をコンテナへコピー
- RUN: Docker環境の準備中にlinuxコマンドを実行する
- EXPOSE: アプリが使うポート番号の宣言
- ENTRYPOINT: サーバーが実際に起動する瞬間に実行されるコマンド