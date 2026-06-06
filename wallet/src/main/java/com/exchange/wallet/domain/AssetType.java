package com.exchange.wallet.domain;

public enum AssetType {
    // ===== Crypto =====
    BTC,    // Bitcoin
    ETH,    // Ethereum
    USDT,   // Tether (ERC20/TRC20)
    BNB,    // Binance Coin
    XRP,    // Ripple
    ADA,    // Cardano
    SOL,    // Solana
    DOGE,   // Dogecoin
    DOT,    // Polkadot
    MATIC,  // Polygon
    LTC,    // Litecoin
    TRX,    // Tron
    AVAX,   // Avalanche

    // ===== Stablecoins =====
    USDC,   // USD Coin
    DAI,    // Dai

    // ===== Fiat =====
    EUR,
    USD,
    GBP,
    CHF
}
