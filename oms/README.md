# OMS - Order Management System

## Overview

The Order Management System (OMS) is responsible for managing the lifecycle of trading orders in the exchange platform. It handles order creation, validation, modification, and cancellation.

## Features

- Order creation and validation
- Order status tracking
- Order modification and cancellation
- Order history management
- Risk management integration

## Technology

- Spring Boot 3.2.0
- Java 21

## Running the Service

```bash
./gradlew :oms:bootRun
```

## Testing

```bash
./gradlew :oms:test
```

## API Endpoints

(To be documented as endpoints are implemented)

## Configuration

The service can be configured through `application.properties` or `application.yml` in the resources directory.
