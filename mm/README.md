# MM - Market Maker

## Overview

The Market Maker service provides automated liquidity to the exchange by continuously placing buy and sell orders, ensuring smooth trading and reducing price volatility.

## Features

- Automated order placement
- Spread management
- Inventory management
- Multiple trading strategies
- Risk controls
- Performance monitoring

## Technology

- Spring Boot 3.2.0
- Java 21

## Running the Service

```bash
./gradlew :mm:bootRun
```

## Testing

```bash
./gradlew :mm:test
```

## Trading Strategies

The market maker supports various strategies including:
- Simple spread strategy
- Inventory-based pricing
- Volatility-adaptive spreads

## Risk Management

Built-in risk controls to manage exposure and prevent excessive losses.

## Configuration

Strategy parameters and risk limits can be configured through application properties.
