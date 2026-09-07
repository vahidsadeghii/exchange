package com.exchange.core.service;

import com.exchange.core.domain.ErrorCode;
import com.exchange.core.sbe.*;
import com.exchange.me.domain.Order;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;


public class RequestHandlerService {
    private final PutOrderDecoder putOrderDecoder;
    private final OrderInfoEncoder orderInfoEncoder;
    private final MessageHeaderEncoder messageHeaderEncoder;
    private final ErrorMessageEncoder errorMessageEncoder;
    private final GetOrderInfoDecoder getOrderInfoDecoder;
    private final CancelOrderDecoder cancelOrderDecoder;
    private final OrderBookDepthDecoder orderBookDepthDecoder;
    private final MarketDepthEncoder marketDepthEncoder;

    private final EngineService engineService;

    public RequestHandlerService() {
        putOrderDecoder = new PutOrderDecoder();
        orderInfoEncoder = new OrderInfoEncoder();
        messageHeaderEncoder = new MessageHeaderEncoder();
        errorMessageEncoder = new ErrorMessageEncoder();
        getOrderInfoDecoder = new GetOrderInfoDecoder();
        cancelOrderDecoder = new CancelOrderDecoder();
        orderBookDepthDecoder = new OrderBookDepthDecoder();
        marketDepthEncoder = new MarketDepthEncoder();

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

    public int handleCancelOrder(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {
        cancelOrderDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        long orderId = cancelOrderDecoder.orderId();
        TradePair tradePair = cancelOrderDecoder.tradePair();

        Order order;
        try {
            order = engineService.cancelOrder(orderId, tradePair);
        } catch (Exception e) {
            order = null;
        }

        if (order != null) {
            orderInfoEncoder
                    .wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                    .correlationId(cancelOrderDecoder.correlationId())
                    .orderId(order.getId())
                    .timestamp(order.getTimestamp())
                    .userId(order.getUserId())
                    .matchStatus(order.getMatchStatus())
                    .filledQuantity(order.getQuantity() - order.getRemainingQuantity());

            return orderInfoEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
        } else {
            return returnErrorMessage(respondBuffer, cancelOrderDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
        }
    }


    public int handleOrderBookDepth(
        long sessionId,
        long timestamp,
        DirectBuffer buffer,
        int offset,
        int headerLength,
        int actingLength,
        int actingVersion,
        ExpandableDirectByteBuffer respondBuffer) {

    orderBookDepthDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

    int depth = orderBookDepthDecoder.depth();
    TradePair pair = orderBookDepthDecoder.pair();

    OrderBookHandler.MarketDepth marketDepth;

    try {
        marketDepth = engineService.getMarketDepth(pair, depth);
    } catch (Exception e) {
        marketDepth = null;
    }

    System.out.println("OrderBookDepth request received");

    if (marketDepth != null) {
        marketDepthEncoder.wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                .correlationId(orderBookDepthDecoder.correlationId());

        MarketDepthEncoder.BidsEncoder bidsEncoder = marketDepthEncoder.bidsCount(marketDepth.bids().size());

        for (OrderBookHandler.PriceLevel bid : marketDepth.bids()) {
            bidsEncoder.next()
                    .price(bid.price())
                    .volume(bid.volume())
                    .orderCount(bid.orderCount());
        }
        // ensure header count matches actual written elements (safety)
        //bidsEncoder.resetCountToIndex();

        MarketDepthEncoder.AsksEncoder asksEncoder = marketDepthEncoder.asksCount(marketDepth.asks().size());

        for (OrderBookHandler.PriceLevel ask : marketDepth.asks()) {
            asksEncoder.next()
                    .price(ask.price())
                    .volume(ask.volume())
                    .orderCount(ask.orderCount());
        }
        // ensure header count matches actual written elements (safety)
      //  asksEncoder.resetCountToIndex();

        return marketDepthEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
    } else {
        return returnErrorMessage(respondBuffer, orderBookDepthDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
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
