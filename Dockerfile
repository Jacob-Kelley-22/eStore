# Build stage
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.jts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY . .
RUN gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]