package com.exchange.wallet.controller.transactioninfo.findtransaction;


import com.exchange.wallet.domain.TransactionInfo;
import com.exchange.wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FindTransactionByWalletIdController {
    private final TransactionService transactionService;

    @PostMapping(value = "${api.prefix.secure}/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<TransactionInfo> handle(@RequestParam("walletId") String walletId) {
        return transactionService.findTransactions(walletId);
    }
}
