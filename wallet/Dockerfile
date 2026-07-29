# Multi-stage build for Wallet module
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy gradle wrapper and build files
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle ./

# Copy module-specific files
COPY wallet/build.gradle wallet/

# Copy source code
COPY wallet/src wallet/src/

# Build the application
RUN ./gradlew clean :wallet:bootJar -x test

# Final stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
#RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Install packages as root
RUN apk add --no-cache wget

# Create application user and log directory
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Copy built jar from builder
COPY --from=builder /build/wallet/build/libs/*.jar app.jar

# Change ownership to appuser
RUN chown appuser:appgroup /app/app.jar

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
