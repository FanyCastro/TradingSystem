package com.example.trading_system.service;

import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DefaultTradeMatcher implements TradeMatcher {
    @Override
    public List<Trade> match(OrderBook orderBook, String instrumentId) {
        List<Trade> trades = new ArrayList<>();

        while (orderBook.hasMatchingOrders()) {
            Order buyOrder = orderBook.getBestBuyOrder().get();
            Order sellOrder = orderBook.getBestSellOrder().get();

            // Skip cancelled orders
            if (buyOrder.getStatus() == Order.OrderStatus.CANCELLED || buyOrder.getQuantity() == 0) {
                orderBook.removeOrder(buyOrder);
                continue;
            }
            if (sellOrder.getStatus() == Order.OrderStatus.CANCELLED || sellOrder.getQuantity() == 0) {
                orderBook.removeOrder(sellOrder);
                continue;
            }

            if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0 &&
                    !buyOrder.getTraderId().equals(sellOrder.getTraderId())) {
                int executedQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                BigDecimal executionPrice = sellOrder.getPrice(); // Use the sell price as the trade price

                // Create trade
                String tradeId = java.util.UUID.randomUUID().toString();
                LocalDateTime timestamp = java.time.LocalDateTime.now();
                Trade trade = new Trade(tradeId, buyOrder.getOrderId(), sellOrder.getOrderId(),
                        instrumentId, executionPrice, executedQuantity, timestamp);
                trades.add(trade);

                // Actualizar las órdenes
                buyOrder.execute(executedQuantity);
                sellOrder.execute(executedQuantity);

                // Eliminar órdenes completadas del libro
                if (buyOrder.isFilled()) {
                    orderBook.removeOrder(buyOrder);
                }
                if (sellOrder.isFilled()) {
                    orderBook.removeOrder(sellOrder);
                }
            } else {
                break; // No hay más órdenes que puedan emparejarse
            }
        }

        return trades;
    }
}
