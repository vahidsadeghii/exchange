package com.exchange.wallet.controller.wallet.updatewallet;


import com.exchange.wallet.controller.wallet.AssetDTO;

import java.util.List;

public record UpdateWalletRequest(List<AssetDTO> assets) {
}
