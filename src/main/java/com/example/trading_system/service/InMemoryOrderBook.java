package com.example.trading_system.service;

import com.example.trading_system.exception.TradingException;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

public class InMemoryOrderBook implements OrderBook{
    // Priority queue for buy orders: highest price first, then earliest timestamp
    private final PriorityBlockingQueue<Order> buyOrders;
    // Priority queue for sale orders: lowest price first, then earliest timestamp
    private final PriorityBlockingQueue<Order> sellOrders;
    // Map to quickly find and cancel orders by ID
    private final Map<String, Order> allOrders;
    // List of executed trades
    private final List<Trade> trades;

    private final String instrumentId;
    private final TradeMatcher tradeMatcher;
    private final MarketPriceCalculator marketPriceCalculator;

    public InMemoryOrderBook(String instrumentId) {
        this.instrumentId = instrumentId;
        this.tradeMatcher = new DefaultTradeMatcher();
        this.marketPriceCalculator = new MidPriceCalculator();
        // Buy orders: higher price first, then earlier timestamp
        this.buyOrders = new PriorityBlockingQueue<>(100, 
                Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getTimestamp));
        // Sell orders: lower price first, then earlier timestamp
        this.sellOrders = new PriorityBlockingQueue<>(100,
                Comparator.comparing(Order::getPrice).thenComparing(Order::getTimestamp));
        this.allOrders = new ConcurrentHashMap<>();
        this.trades = new CopyOnWriteArrayList<>();
    }


    @Override
    public void addOrder(Order order) {
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.offer(order);
        } else {
            sellOrders.offer(order);
        }
        allOrders.put(order.getOrderId(), order);
    }

    @Override
    public void cancelOrder(String orderId) {
        Order order = allOrders.get(orderId);
        if (order == null)
            throw new TradingException(TradingException.ErrorCode.ORDER_NOT_FOUND.name(),
                    "Order not found: " + orderId);
        if (order.getStatus() == Order.OrderStatus.CANCELLED)
            return;

        order.setStatus(Order.OrderStatus.CANCELLED);
        // Remove from the appropriate queue but keep in allOrders
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
    }

    @Override
    public boolean hasMatchingOrders() {
        Optional<Order> buyOrder = getBestBuyOrder();
        Optional<Order> sellOrder = getBestSellOrder();
        return buyOrder.isPresent() && sellOrder.isPresent()
                && buyOrder.get().getPrice().compareTo(sellOrder.get().getPrice()) >= 0;
    }

    @Override
    public List<Trade> matchOrders() {
        return tradeMatcher.match(this, instrumentId);
    }

    @Override
    public Optional<BigDecimal> getMarketPrice() {
        return marketPriceCalculator.calculateMarketPrice(this);
    }

    @Override
    public Optional<Order> getBestBuyOrder() {
        return Optional.ofNullable(buyOrders.peek());
    }

    @Override
    public Optional<Order> getBestSellOrder() {
        return Optional.ofNullable(sellOrders.peek());
    }

    @Override
    public List<Order> getSellOrders() {
        return new ArrayList<>(sellOrders);
    }

    @Override
    public List<Order> getBuyOrders() {
        return new ArrayList<>(buyOrders);
    }

    @Override
    public Map<String, Order> getAllOrders() {
        return allOrders;
    }

    @Override
    public void removeOrder(Order order) {
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
    }
}
