package com.exchange.wallet.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssetType {

    // ===== Cryptocurrencies =====

    BTC("Bitcoin", 8),          // Bitcoin
    ETH("Ethereum", 18),        // Ethereum
    USDT("Tether", 6),          // Tether (USD Stablecoin)
    BNB("Binance Coin", 18),    // Binance Coin
    XRP("Ripple", 6),           // Ripple
    ADA("Cardano", 6),          // Cardano
    SOL("Solana", 9),           // Solana
    DOGE("Dogecoin", 8),        // Dogecoin
    DOT("Polkadot", 10),        // Polkadot
    MATIC("Polygon", 18),       // Polygon (formerly Matic)
    LTC("Litecoin", 8),         // Litecoin
    TRX("TRON", 6),             // TRON
    AVAX("Avalanche", 18),      // Avalanche

    // ===== Stablecoins =====

    USDC("USD Coin", 6),        // USD Coin
    DAI("Dai", 18),             // Dai Stablecoin

    // ===== Fiat Currencies =====

    EUR("Euro", 2),             // Euro
    USD("US Dollar", 2),        // United States Dollar
    GBP("British Pound", 2),    // British Pound Sterling
    CHF("Swiss Franc", 2);      // Swiss Franc

    /**
     * Human-readable asset name.
     */
    private final String displayName;

    /**
     * Number of decimal places supported by the asset.
     * Examples:
     * - BTC -> 8
     * - ETH -> 18
     * - EUR/USD -> 2
     */
    private final int scale;
}