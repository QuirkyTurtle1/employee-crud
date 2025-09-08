# ---------- build stage ----------
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version

COPY src ./src

RUN ./gradlew bootJar -x test

# ---------- run stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app:app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    LOG_LEVEL_ROOT=INFO \
    LOG_LEVEL_APP=INFO

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
