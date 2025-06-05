package com.example.trading_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a buy or sell order placed by a trader.
 */
public class Order {
    /**
     * Unique identifier for the order.
     */
    private String orderId;

    /**
     * Identifier of the trader who placed the order.
     */
    private String traderId;

    /**
     * Identifier of the instrument this order is for.
     */
    private String instrumentId;

    /**
     * Type of the order: BUY or SELL.
     */
    private OrderType type;

    /**
     * Limit price for the order (can be null for market orders).
     */
    private BigDecimal price;

    /**
     * Quantity to buy or sell.
     */
    private int quantity;

    /**
     * Current status of the order.
     */
    private OrderStatus status;

    /**
     * Timestamp when the order was created (used for FIFO priority).
     */
    private LocalDateTime timestamp;

    public Order(String traderId, String instrumentId, OrderType type, BigDecimal price, int quantity) {
        this.orderId = UUID.randomUUID().toString();
        this.traderId = traderId;
        this.instrumentId = instrumentId;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.status = OrderStatus.OPEN;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTraderId() {
        return traderId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Enum representing the type of the order: BUY or SELL.
     */
    public enum OrderType {
        BUY,
        SELL
    }

    /**
     * Enum representing the status of the order.
     */
    public enum OrderStatus {
        OPEN,
        PARTIALLY_FILLED,
        FILLED,
        CANCELLED
    }
}
