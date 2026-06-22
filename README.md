# Exchange Platform

A comprehensive cryptocurrency exchange platform built with Spring Boot and microservices architecture.

## Overview

This project is a modern cryptocurrency exchange platform designed to provide secure, scalable, and efficient trading services. The platform is built using a microservices architecture, with each service handling specific business domains.

## Architecture

The platform consists of the following modules:

- **OMS (Order Management System)** - Handles order processing and management
- **ME (Matching Engine)** - Core trading engine for order matching
- **Wallet** - Manages user cryptocurrency wallets and balances
- **DP (Data Provider)** - Provides market data and real-time feeds
- **Profile** - User profile and account management
- **MM (Market Maker)** - Automated market making service
- **CE (Crypto Exchange)** - External crypto exchange integration

## Technology Stack

- **Java 21** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Gradle** - Build tool
- **JUnit 5** - Testing framework

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 9.0.0 or higher

### Building the Project

```bash
./gradlew build
```

### Running a Module

Each module can be run independently:

```bash
./gradlew :oms:bootRun
./gradlew :me:bootRun
./gradlew :wallet:bootRun
# ... and so on
```

### Running Tests

```bash
./gradlew test
```

### Building Docker Images

Each module can be containerized using the provided `build-docker.sh` script.

#### Prerequisites

- Docker installed and running

#### Build a Single Module

Build a Docker image for any module:

```bash
# Build CE module with latest tag
./build-docker.sh ce

# Build Gateway module with specific version tag
./build-docker.sh gateway v1.0

# Build OMS module
./build-docker.sh oms
```

#### Available Modules for Docker Build

- ce
- gateway
- oms
- me
- wallet
- dp
- emailmanager
- profile
- mm

#### Running Docker Containers

After building, run a container:

```bash
# Run CE module
docker run -p 8080:8080 exchange-ce:latest

# Run Gateway with environment variables
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  exchange-gateway:latest

# Run with volume for logs
docker run -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  exchange-oms:latest
```

#### View Built Images

```bash
# List all exchange images
docker images 'exchange-*'

# View specific image details
docker inspect exchange-ce:latest
```

#### Docker Image Details

- **Base Image**: Eclipse Temurin 21 JRE Alpine (minimal, secure)
- **Security**: Runs as non-root user
- **Health Check**: Enabled with 30-second intervals
- **Default Port**: 8080
- **Size**: Minimal due to Alpine base and multi-stage builds

## Project Structure

```
exchange/
├── oms/           # Order Management System
├── me/            # Matching Engine
├── wallet/        # Wallet Service
├── dp/            # Data Provider
├── profile/       # Profile Service
├── mm/            # Market Maker
├── ce/            # Crypto Exchange
└── gradle/        # Gradle configuration
```

## License

Copyright © 2025 Exchange Platform
