package com.example.trading_system.service;

import com.example.trading_system.exception.TradingException;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

public class InMemoryOrderBook implements OrderBook{
    private static final Logger log = LoggerFactory.getLogger(InMemoryOrderBook.class);

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
        log.info("Initializing order book for instrument: {}", instrumentId);
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
        log.debug("Order book initialized with capacity 100 for both buy and sell queues");
    }

    @Override
    public void addOrder(Order order) {
        log.info("Adding order {} for instrument {}: type={}, price={}, quantity={}", 
            order.getOrderId(), instrumentId, order.getType(), order.getPrice(), order.getQuantity());
        
        boolean added = false;
        if (order.getType() == Order.OrderType.BUY) {
            added = buyOrders.offer(order);
        } else {
            added = sellOrders.offer(order);
        }
        
        if (!added) {
            log.error("Failed to add order {} to queue - queue is full", order.getOrderId());
            throw new TradingException(TradingException.ErrorCode.ORDER_QUEUE_FULL.name(),
                "Order queue is full: " + order.getOrderId());
        }
        
        allOrders.put(order.getOrderId(), order);
        log.debug("Order {} successfully added to {} queue", order.getOrderId(), order.getType());
    }

    @Override
    public void cancelOrder(String orderId) {
        log.info("Attempting to cancel order {} for instrument {}", orderId, instrumentId);
        
        Order order = allOrders.get(orderId);
        if (order == null) {
            log.warn("Order {} not found for cancellation", orderId);
            throw new TradingException(TradingException.ErrorCode.ORDER_NOT_FOUND.name(),
                    "Order not found: " + orderId);
        }
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            log.info("Order {} was already cancelled", orderId);
            return;
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        log.debug("Order {} status set to CANCELLED", orderId);

        // Remove from the appropriate queue but keep in allOrders
        boolean removed = false;
        if (order.getType() == Order.OrderType.BUY) {
            removed = buyOrders.remove(order);
        } else {
            removed = sellOrders.remove(order);
        }
        
        if (!removed) {
            log.warn("Order {} was already removed from queue", orderId);
        } else {
            log.debug("Order {} removed from {} queue", orderId, order.getType());
        }
    }

    @Override
    public boolean hasMatchingOrders() {
        Optional<Order> buyOrder = getBestBuyOrder();
        Optional<Order> sellOrder = getBestSellOrder();
        boolean hasMatch = buyOrder.isPresent() && sellOrder.isPresent()
                && buyOrder.get().getPrice().compareTo(sellOrder.get().getPrice()) >= 0;
        
        if (hasMatch) {
            log.debug("Found matching orders - Buy: {} at {}, Sell: {} at {}", 
                buyOrder.get().getOrderId(), buyOrder.get().getPrice(),
                sellOrder.get().getOrderId(), sellOrder.get().getPrice());
        }
        return hasMatch;
    }

    @Override
    public List<Trade> matchOrders() {
        log.info("Starting order matching process for instrument {}", instrumentId);
        List<Trade> tradeList = tradeMatcher.match(this, instrumentId);
        this.trades.addAll(tradeList);
        
        if (!tradeList.isEmpty()) {
            log.info("Matched {} trades for instrument {}", tradeList.size(), instrumentId);
            tradeList.forEach(trade -> 
                log.debug("Trade executed: {} - Buy: {}, Sell: {}, Price: {}, Quantity: {}", 
                    trade.tradeId(), trade.buyOrderId(), trade.sellOrderId(), 
                    trade.price(), trade.quantity()));
        } else {
            log.debug("No trades matched for instrument {}", instrumentId);
        }
        
        return tradeList;
    }

    @Override
    public Optional<BigDecimal> getMarketPrice() {
        Optional<BigDecimal> price = marketPriceCalculator.calculateMarketPrice(this);
        price.ifPresent(p -> log.debug("Current market price for {}: {}", instrumentId, p));
        return price;
    }

    @Override
    public Optional<Order> getBestBuyOrder() {
        Optional<Order> order = Optional.ofNullable(buyOrders.peek());
        order.ifPresent(o -> log.debug("Best buy order for {}: {} at {}", 
            instrumentId, o.getOrderId(), o.getPrice()));
        return order;
    }

    @Override
    public Optional<Order> getBestSellOrder() {
        Optional<Order> order = Optional.ofNullable(sellOrders.peek());
        order.ifPresent(o -> log.debug("Best sell order for {}: {} at {}", 
            instrumentId, o.getOrderId(), o.getPrice()));
        return order;
    }

    @Override
    public List<Order> getBuyOrders() {
        List<Order> orders = new ArrayList<>(buyOrders);
        log.debug("Retrieved {} buy orders for {}", orders.size(), instrumentId);
        return orders;
    }

    @Override
    public List<Order> getSellOrders() {
        List<Order> orders = new ArrayList<>(sellOrders);
        log.debug("Retrieved {} sell orders for {}", orders.size(), instrumentId);
        return orders;
    }

    @Override
    public Map<String, Order> getAllOrders() {
        log.debug("Retrieved {} total orders for {}", allOrders.size(), instrumentId);
        return allOrders;
    }

    @Override
    public void removeOrder(Order order) {
        log.debug("Removing order {} from {} queue", order.getOrderId(), order.getType());
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
    }
}
