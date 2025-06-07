package com.example.trading_system.service;

import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderBook {
    void addOrder(Order order);
    void removeOrder(Order order);
    void cancelOrder(String orderId);
    boolean hasMatchingOrders();

    List<Trade> matchOrders();
    Optional<BigDecimal> getMarketPrice();

    Optional<Order> getBestBuyOrder();
    Optional<Order> getBestSellOrder();
    List<Order> getSellOrders();
    List<Order> getBuyOrders();
    Map<String, Order> getAllOrders();
}