# ME - Matching Engine

## Overview

The Matching Engine is the core trading component of the exchange platform. It matches buy and sell orders based on price-time priority algorithm, ensuring fair and efficient trade execution.

## Features

- Real-time order matching
- Price-time priority algorithm
- High-performance order book management
- Trade execution and settlement
- Market and limit order support

## Technology

- Spring Boot 3.2.0
- Java 21

## Running the Service

```bash
./gradlew :me:bootRun
```

## Testing

```bash
./gradlew :me:test
```

## Performance

The matching engine is designed for low-latency, high-throughput order processing to support active trading markets.

## Configuration

Configuration options for order matching algorithms and performance tuning can be set in the application properties.
