package com.exchange.coregateway.dto;




import java.util.List;

public record CreateWalletRequest(List<Asset> assets, long userId) {
}
