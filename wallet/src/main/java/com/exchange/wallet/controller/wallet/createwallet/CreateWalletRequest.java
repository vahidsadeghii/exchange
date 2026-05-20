package com.exchange.wallet.controller.wallet.createwallet;


import java.util.List;

public record CreateWalletRequest (List<AssetDTO> assets) {
}
