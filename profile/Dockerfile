# Multi-stage build for Profile module
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Gradle wrapper and build files
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle ./

# Copy module build file
COPY profile/build.gradle profile/

# Copy source code
COPY profile/src profile/src/

# Build application
RUN ./gradlew clean :profile:bootJar -x test


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user/group
RUN addgroup -S appgroup \
    && adduser -S appuser -G appgroup

# Create log directory and give ownership to appuser
RUN mkdir -p /log \
    && chown -R appuser:appgroup /log

# Copy ONLY the executable Spring Boot jar
COPY --from=builder /build/profile/build/libs/*.jar /app/app.jar

# Give appuser ownership of application
RUN chown appuser:appgroup /app/app.jar

USER appuser

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "/app/app.jar"]