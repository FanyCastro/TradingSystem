package com.example.tradingSystem.service;

import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;

import java.math.BigDecimal;
import java.util.*;

/**
 * OrderBook manages buy and sell orders for a single financial instrument.
 * It provides methods to add, cancel, and match orders, as well as retrieve the best buy/sell orders.
 */
public class OrderBook {
    // Priority queue for buy orders: highest price first, then earliest timestamp
    private PriorityQueue<Order> buyOrders;
    // Priority queue for sell orders: lowest price first, then earliest timestamp
    private PriorityQueue<Order> sellOrders;
    // Map to quickly find and cancel orders by ID
    private Map<String, Order> allOrders;
    // List of executed trades
    private List<Trade> trades;
    // Instrument ID this order book is for
    private final String instrumentId;

    public OrderBook(String instrumentId) {
        this.instrumentId = instrumentId;
        // Buy orders: higher price first, then earlier timestamp
        this.buyOrders = new PriorityQueue<>((o1, o2) -> {
            int priceCmp = o2.getPrice().compareTo(o1.getPrice());
            if (priceCmp != 0) return priceCmp;
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        });
        // Sell orders: lower price first, then earlier timestamp
        this.sellOrders = new PriorityQueue<>((o1, o2) -> {
            int priceCmp = o1.getPrice().compareTo(o2.getPrice());
            if (priceCmp != 0) return priceCmp;
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        });
        this.allOrders = new HashMap<>();
        this.trades = new ArrayList<>();
    }

    /**
     * Adds an order to the order book.
     * @param order The order to add.
     */
    public void addOrder(Order order) {
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.offer(order);
        } else {
            sellOrders.offer(order);
        }
        allOrders.put(order.getOrderId(), order);
    }

    /**
     * Cancels an order by its ID.
     * @param orderId The ID of the order to cancel.
     * @return true if the order was found and cancelled, false otherwise.
     */
    public boolean cancelOrder(String orderId) {
        Order order = allOrders.get(orderId);
        if (order == null) return false;
        order.setStatus(Order.OrderStatus.CANCELLED);
        // Remove from the appropriate queue
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
        allOrders.remove(orderId);
        return true;
    }

    /**
     * Matches buy and sell orders and executes trades at the best available price.
     * Trades are executed when the best buy price >= best sell price.
     * @return List of executed trades.
     */
    public List<Trade> matchOrders() {
        List<Trade> executedTrades = new ArrayList<>();
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buy = buyOrders.peek();
            Order sell = sellOrders.peek();
            // Only match if buy price >= sell price
            if (buy.getPrice().compareTo(sell.getPrice()) >= 0) {
                int tradeQty = Math.min(buy.getQuantity(), sell.getQuantity());
                BigDecimal tradePrice = sell.getPrice(); // Use sell price as market price
                // Create tradeId and timestamp for the Trade record
                String tradeId = java.util.UUID.randomUUID().toString();
                java.time.LocalDateTime timestamp = java.time.LocalDateTime.now();
                // Create trade using the record constructor
                Trade trade = new Trade(tradeId, buy.getOrderId(), sell.getOrderId(), instrumentId, tradePrice, tradeQty, timestamp);
                executedTrades.add(trade);
                trades.add(trade);
                // Update order quantities
                buy.setQuantity(buy.getQuantity() - tradeQty);
                sell.setQuantity(sell.getQuantity() - tradeQty);
                // Update order status
                if (buy.getQuantity() == 0) {
                    buy.setStatus(Order.OrderStatus.FILLED);
                    buyOrders.poll();
                    allOrders.remove(buy.getOrderId());
                } else {
                    buy.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
                }
                if (sell.getQuantity() == 0) {
                    sell.setStatus(Order.OrderStatus.FILLED);
                    sellOrders.poll();
                    allOrders.remove(sell.getOrderId());
                } else {
                    sell.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
                }
            } else {
                // No more matches possible
                break;
            }
        }
        return executedTrades;
    }

    /**
     * Gets the best buy order (highest price).
     * @return The best buy order, or null if none.
     */
    public Order getBestBuyOrder() {
        return buyOrders.peek();
    }

    /**
     * Gets the best sell order (lowest price).
     * @return The best sell order, or null if none.
     */
    public Order getBestSellOrder() {
        return sellOrders.peek();
    }

    /**
     * Gets all open buy orders (for display or debugging).
     */
    public List<Order> getBuyOrders() {
        return new ArrayList<>(buyOrders);
    }

    /**
     * Gets all open sell orders (for display or debugging).
     */
    public List<Order> getSellOrders() {
        return new ArrayList<>(sellOrders);
    }

    /**
     * Gets all executed trades.
     */
    public List<Trade> getTrades() {
        return trades;
    }
}
