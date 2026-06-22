# Multi-stage build for OMS module
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy gradle wrapper and build files
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle ./

# Copy module-specific files
COPY oms/build.gradle oms/

# Copy source code
COPY oms/src oms/src/

# Build the application
RUN ./gradlew clean :oms:bootJar -x test

# Final stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built jar from builder
COPY --from=builder /build/oms/build/libs/*.jar app.jar

# Change ownership to appuser
RUN chown appuser:appgroup /app/app.jar

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
