package com.exchange.wallet.controller.wallet.createwallet;

import com.exchange.wallet.controller.wallet.AssetDTO;
import java.util.List;

public record CreateWalletRequest(List<AssetDTO> assets, long userId) {
}
