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
