package com.exchange.core.service;

import com.exchange.core.domain.ErrorCode;
import com.exchange.core.sbe.ErrorMessageEncoder;
import com.exchange.core.sbe.GetOrderInfoDecoder;
import com.exchange.core.sbe.MessageHeaderEncoder;
import com.exchange.core.sbe.OrderInfoEncoder;
import com.exchange.core.sbe.PutOrderDecoder;
import com.exchange.me.domain.Order;
import com.exchange.me.service.EngineService;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;

public class RequestHandlerService {
    private final PutOrderDecoder putOrderDecoder;
    private final OrderInfoEncoder orderInfoEncoder;
    private final MessageHeaderEncoder messageHeaderEncoder;
    private final ErrorMessageEncoder errorMessageEncoder;
    private final GetOrderInfoDecoder getOrderInfoDecoder;

    private final EngineService engineService;

    public RequestHandlerService() {
        putOrderDecoder = new PutOrderDecoder();
        orderInfoEncoder = new OrderInfoEncoder();
        messageHeaderEncoder = new MessageHeaderEncoder();
        errorMessageEncoder = new ErrorMessageEncoder();
        getOrderInfoDecoder = new GetOrderInfoDecoder();

        engineService = new EngineService();
    }

    public int handlePutOrderRequest(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {
        putOrderDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        var order =
                engineService.createUpdateOrder(
                        null,
                        putOrderDecoder.orderId(),
                        putOrderDecoder.userId(),
                        putOrderDecoder.timestamp(),
                        putOrderDecoder.tradeSide(),
                        putOrderDecoder.tradePair(),
                        putOrderDecoder.orderType(),
                        putOrderDecoder.marketType(),
                        putOrderDecoder.quantity(),
                        putOrderDecoder.price());

        if (order != null) {
            orderInfoEncoder
                    .wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                    .correlationId(putOrderDecoder.correlationId())
                    .orderId(order.getId())
                    .timestamp(order.getTimestamp())
                    .userId(order.getUserId())
                    .matchStatus(order.getMatchStatus())
                    .filledQuantity(order.getQuantity() - order.getRemainingQuantity());

            return orderInfoEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
        } else {
            return returnErrorMessage(respondBuffer, putOrderDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
        }
    }

    public int handleGetOrderInfo(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {
        getOrderInfoDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        Order order;
        try {
            order =
                    engineService.getOrder(getOrderInfoDecoder.tradePair(), getOrderInfoDecoder.orderId());
        } catch (Exception e) {
            order = null;
        }

        if (order != null) {
            orderInfoEncoder
                    .wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                    .correlationId(getOrderInfoDecoder.correlationId())
                    .orderId(order.getId())
                    .timestamp(order.getTimestamp())
                    .userId(order.getUserId())
                    .matchStatus(order.getMatchStatus())
                    .filledQuantity(order.getQuantity() - order.getRemainingQuantity());

            return orderInfoEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
        } else {
            return returnErrorMessage(respondBuffer, getOrderInfoDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
        }
    }

    private int returnErrorMessage(ExpandableDirectByteBuffer respondBuffer, long correlationId, int errorCode) {
        errorMessageEncoder.wrapAndApplyHeader(
                respondBuffer, 0, messageHeaderEncoder
        );
        errorMessageEncoder.correlationId(correlationId);
        errorMessageEncoder.code(errorCode);

        return errorMessageEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
    }
}
