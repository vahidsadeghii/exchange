# DP - Data Provider

## Overview

The Data Provider service aggregates and distributes market data, price feeds, and trading information to other services and end users.

## Features

- Real-time market data feeds
- Historical price data
- Trading volume and statistics
- Order book snapshots
- Ticker information
- WebSocket support for live updates

## Technology

- Spring Boot 3.2.0
- Java 21

## Running the Service

```bash
./gradlew :dp:bootRun
```

## Testing

```bash
./gradlew :dp:test
```

## Data Sources

The service aggregates data from multiple sources including the internal matching engine and external market data providers.

## API Endpoints

(To be documented as endpoints are implemented)
