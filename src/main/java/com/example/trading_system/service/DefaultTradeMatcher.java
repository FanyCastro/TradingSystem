package com.example.trading_system.service;

import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultTradeMatcher implements TradeMatcher {
    @Override
    public List<Trade> match(OrderBook orderBook, String instrumentId) {
        List<Trade> trades = new ArrayList<>();

        while (orderBook.hasMatchingOrders()) {
            Optional<Order> buyOrderOpt = orderBook.getBestBuyOrder();
            Optional<Order> sellOrderOpt = orderBook.getBestSellOrder();
            
            if (!buyOrderOpt.isPresent() || !sellOrderOpt.isPresent()) break;
            
            Order buyOrder = buyOrderOpt.get();
            Order sellOrder = sellOrderOpt.get();

            if (shouldSkipOrder(buyOrder, orderBook) || shouldSkipOrder(sellOrder, orderBook)) {
                continue;
            }

            if (!canMatch(buyOrder, sellOrder)) {
                break;
            }

            Trade trade = createTrade(buyOrder, sellOrder, instrumentId);
            trades.add(trade);
            updateOrders(buyOrder, sellOrder, orderBook);
        }

        return trades;
    }

    private boolean shouldSkipOrder(Order order, OrderBook orderBook) {
        if (order.getStatus() == Order.OrderStatus.CANCELLED || order.getQuantity() == 0) {
            orderBook.removeOrder(order);
            return true;
        }
        return false;
    }

    private boolean canMatch(Order buyOrder, Order sellOrder) {
        return buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0 
            && !buyOrder.getTraderId().equals(sellOrder.getTraderId());
    }

    private Trade createTrade(Order buyOrder, Order sellOrder, String instrumentId) {
        int executedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        BigDecimal executionPrice = sellOrder.getPrice();
        String tradeId = java.util.UUID.randomUUID().toString();
        LocalDateTime timestamp = java.time.LocalDateTime.now();
        
        return new Trade(tradeId, buyOrder.getOrderId(), sellOrder.getOrderId(),
                instrumentId, executionPrice, executedQuantity, timestamp);
    }

    private void updateOrders(Order buyOrder, Order sellOrder, OrderBook orderBook) {
        int executedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        buyOrder.execute(executedQuantity);
        sellOrder.execute(executedQuantity);

        if (buyOrder.isFilled()) {
            orderBook.removeOrder(buyOrder);
        }
        if (sellOrder.isFilled()) {
            orderBook.removeOrder(sellOrder);
        }
    }
}
