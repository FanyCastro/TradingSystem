package com.example.trading_system.service;

import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeMatcher {
    private static final Logger log = LoggerFactory.getLogger(TradeMatcher.class);

    public List<Trade> match(OrderBook orderBook, String instrumentId) {
        log.info("Starting order matching process for instrument {}", instrumentId);
        List<Trade> trades = new ArrayList<>();

        while (shouldContinueMatching(orderBook)) {
            Optional<Order> buyOrderOpt = orderBook.getBestBuyOrder();
            Optional<Order> sellOrderOpt = orderBook.getBestSellOrder();
            
            if (buyOrderOpt.isEmpty() || sellOrderOpt.isEmpty()) {
                log.debug("No more matching orders available");
                return trades;
            }
            
            Order buyOrder = buyOrderOpt.get();
            Order sellOrder = sellOrderOpt.get();

            log.debug("Evaluating potential match - Buy: {} at {}, Sell: {} at {}", 
                buyOrder.getOrderId(), buyOrder.getPrice(),
                sellOrder.getOrderId(), sellOrder.getPrice());

            if (shouldSkipOrder(buyOrder, orderBook) || shouldSkipOrder(sellOrder, orderBook)) {
                log.debug("Skipping order due to cancellation or zero quantity");
                continue;
            }

            if (!canMatch(buyOrder, sellOrder)) {
                log.debug("Orders cannot be matched - Buy: {}, Sell: {}", 
                    buyOrder.getOrderId(), sellOrder.getOrderId());
                return trades;
            }

            Trade trade = createTrade(buyOrder, sellOrder, instrumentId);
            trades.add(trade);
            log.info("Created trade {} between buy order {} and sell order {} - Price: {}, Quantity: {}", 
                trade.tradeId(), buyOrder.getOrderId(), sellOrder.getOrderId(),
                trade.price(), trade.quantity());

            updateOrders(buyOrder, sellOrder, orderBook);
            log.debug("Updated order quantities - Buy: {} remaining {}, Sell: {} remaining {}", 
                buyOrder.getOrderId(), buyOrder.getQuantity(),
                sellOrder.getOrderId(), sellOrder.getQuantity());
        }

        log.info("Completed matching process for instrument {} - {} trades executed", 
            instrumentId, trades.size());
        return trades;
    }

    private boolean shouldSkipOrder(Order order, OrderBook orderBook) {
        if (order.getStatus() == Order.OrderStatus.CANCELLED || order.getQuantity() == 0) {
            log.debug("Skipping order {} - Status: {}, Quantity: {}", 
                order.getOrderId(), order.getStatus(), order.getQuantity());
            orderBook.removeOrder(order);
            return true;
        }
        return false;
    }

    private boolean canMatch(Order buyOrder, Order sellOrder) {
        boolean priceMatch = buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0;
        boolean differentTraders = !buyOrder.getTraderId().equals(sellOrder.getTraderId());
        
        if (!priceMatch) {
            log.debug("Price mismatch - Buy: {}, Sell: {}", 
                buyOrder.getPrice(), sellOrder.getPrice());
        }
        if (!differentTraders) {
            log.debug("Same trader attempting to match with themselves: {}", 
                buyOrder.getTraderId());
        }
        
        return priceMatch && differentTraders;
    }

    private Trade createTrade(Order buyOrder, Order sellOrder, String instrumentId) {
        int executedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        BigDecimal executionPrice = sellOrder.getPrice();
        String tradeId = java.util.UUID.randomUUID().toString();
        LocalDateTime timestamp = java.time.LocalDateTime.now();
        
        log.debug("Creating trade - Quantity: {}, Price: {}", executedQuantity, executionPrice);
        
        return new Trade(tradeId, buyOrder.getOrderId(), sellOrder.getOrderId(),
                instrumentId, executionPrice, executedQuantity, timestamp);
    }

    private void updateOrders(Order buyOrder, Order sellOrder, OrderBook orderBook) {
        int executedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        
        log.debug("Executing {} units for buy order {} and sell order {}", 
            executedQuantity, buyOrder.getOrderId(), sellOrder.getOrderId());
            
        buyOrder.execute(executedQuantity);
        sellOrder.execute(executedQuantity);

        if (buyOrder.isFilled()) {
            log.debug("Buy order {} is filled, removing from order book", buyOrder.getOrderId());
            orderBook.removeOrder(buyOrder);
        }
        if (sellOrder.isFilled()) {
            log.debug("Sell order {} is filled, removing from order book", sellOrder.getOrderId());
            orderBook.removeOrder(sellOrder);
        }
    }

    private boolean shouldContinueMatching(OrderBook orderBook) {
        return orderBook.hasMatchingOrders();
    }
}
