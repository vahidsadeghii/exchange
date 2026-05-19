package com.exchange.wallet.controller.wallet.createwallet;

import com.exchange.wallet.domain.Asset;

import java.util.List;

public record CreateWalletRequest (List<Asset> assets) {
}
