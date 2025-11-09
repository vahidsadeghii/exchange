# CE - Crypto Exchange Integration

## Overview

The Crypto Exchange service provides integration with external cryptocurrency exchanges, enabling arbitrage opportunities, price discovery, and liquidity aggregation.

## Features

- Multi-exchange connectivity
- Real-time price feeds from external exchanges
- Order routing to external venues
- Arbitrage detection
- Liquidity aggregation
- API key management

## Technology

- Spring Boot 3.2.0
- Java 21

## Running the Service

```bash
./gradlew :ce:bootRun
```

## Testing

```bash
./gradlew :ce:test
```

## Supported Exchanges

(To be documented as integrations are implemented)

## API Integration

The service uses RESTful APIs and WebSocket connections to communicate with external exchanges.

## Configuration

Exchange API credentials and connection settings are configured through secure configuration management.
