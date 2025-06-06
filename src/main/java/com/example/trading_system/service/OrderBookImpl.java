package com.example.trading_system.service;

import com.example.trading_system.exception.TradingException;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.util.*;

/**
 * OrderBook manages buy and sell orders for a single financial instrument.
 * It provides methods to add, cancel, and match orders, as well as retrieve the best buy/sell orders.
 */
public class OrderBookImpl implements OrderBook {

    // Priority queue for buy orders: highest price first, then earliest timestamp
    private final PriorityQueue<Order> buyOrders;
    // Priority queue for sale orders: lowest price first, then earliest timestamp
    private final PriorityQueue<Order> sellOrders;

    // Map to quickly find and cancel orders by ID
    private final Map<String, Order> allOrders;
    // List of executed trades
    private final List<Trade> trades;
    // Instrument ID this order book is for
    private final String instrumentId;

    public OrderBookImpl(String instrumentId) {
        this.instrumentId = instrumentId;
        // Buy orders: higher price first, then earlier timestamp
        this.buyOrders = new PriorityQueue<>(
                Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getTimestamp));
        // Sell orders: lower price first, then earlier timestamp
        this.sellOrders = new PriorityQueue<>(
                Comparator.comparing(Order::getPrice).thenComparing(Order::getTimestamp));
        this.allOrders = new HashMap<>();
        this.trades = new ArrayList<>();
    }

    public void addOrder(Order order) {
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.offer(order);
        } else {
            sellOrders.offer(order);
        }
        allOrders.put(order.getOrderId(), order);
    }

    public boolean cancelOrder(String orderId) {
        Order order = allOrders.get(orderId);
        if (order == null)
            throw new TradingException(TradingException.ErrorCode.ORDER_NOT_FOUND.name(),
                    "Order not found: " + orderId);
        if (order.getStatus() == Order.OrderStatus.CANCELLED)
            return false;

        order.setStatus(Order.OrderStatus.CANCELLED);
        // Remove from the appropriate queue but keep in allOrders
        if (order.getType() == Order.OrderType.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
        return true;
    }

    public List<Trade> matchOrders() {
        List<Trade> trades = new ArrayList<>();
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buyOrder = buyOrders.peek();
            Order sellOrder = sellOrders.peek();

            // Skip cancelled orders
            if (buyOrder.getStatus() == Order.OrderStatus.CANCELLED || buyOrder.getQuantity() == 0) {
                buyOrders.poll();
                continue;
            }
            if (sellOrder.getStatus() == Order.OrderStatus.CANCELLED || sellOrder.getQuantity() == 0) {
                sellOrders.poll();
                continue;
            }

            // Only match if buy price >= sell price and different traders
            if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0 && 
                !buyOrder.getTraderId().equals(sellOrder.getTraderId())) {
                // Match found
                int quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                BigDecimal price = sellOrder.getPrice(); // Use the sell price as the trade price

                // Create trade
                String tradeId = java.util.UUID.randomUUID().toString();
                java.time.LocalDateTime timestamp = java.time.LocalDateTime.now();
                Trade trade = new Trade(tradeId, buyOrder.getOrderId(), sellOrder.getOrderId(), 
                    instrumentId, price, quantity, timestamp);
                trades.add(trade);

                // Update order quantities
                buyOrder.setQuantity(buyOrder.getQuantity() - quantity);
                sellOrder.setQuantity(sellOrder.getQuantity() - quantity);

                // Remove filled orders
                if (buyOrder.getQuantity() == 0) {
                    buyOrders.poll();
                    buyOrder.setStatus(Order.OrderStatus.FILLED);
                }
                if (sellOrder.getQuantity() == 0) {
                    sellOrders.poll();
                    sellOrder.setStatus(Order.OrderStatus.FILLED);
                }
            } else {
                // No more matches possible
                break;
            }
        }
        return trades;
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

    public Map<String, Order> getAllOrders() {
        return allOrders;
    }
}
