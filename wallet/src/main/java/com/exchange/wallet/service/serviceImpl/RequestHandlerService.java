package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.ErrorCode;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.sbe.*;

import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RequestHandlerService {

    private final WalletService walletService;
    private final WalletRequestDecoder walletRequestDecoder = new WalletRequestDecoder();
    private final WalletResponseEncoder walletResponseEncoder = new WalletResponseEncoder();
    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final WalletErrorMessageEncoder walletErrorMessageEncoder = new WalletErrorMessageEncoder();


    public int handleWithdrawWalletRequest(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {

        walletRequestDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        var amountDecoder = walletRequestDecoder.amount();

        Wallet wallet = walletService.withdrawWallet(walletRequestDecoder.walletId(),
                AssetType.valueOf(walletRequestDecoder.assetType().name()),
                BigDecimal.valueOf(amountDecoder.mantissa()).scaleByPowerOfTen(amountDecoder.exponent()));

        if (wallet != null) {

            return walletResponseEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
        } else {
            return returnErrorMessage(respondBuffer, walletRequestDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
        }

    }


    private int returnErrorMessage(ExpandableDirectByteBuffer respondBuffer, long correlationId, int errorCode) {
        walletErrorMessageEncoder.wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder);
        walletErrorMessageEncoder.correlationId(correlationId);
        walletErrorMessageEncoder.code(errorCode);

        return walletErrorMessageEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
    }
}
