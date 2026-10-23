FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/qiita-summary-notifier-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]