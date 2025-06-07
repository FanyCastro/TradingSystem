package com.example.trading_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a buy or sell order placed by a trader.
 */
public class Order {
    private final String orderId;
    private final String instrumentId;
    private final OrderType type;
    private final String traderId;
    private final BigDecimal price;
    private int quantity;
    private OrderStatus status;
    private final LocalDateTime timestamp;

    public Order(String instrumentId, String traderId, OrderType type, BigDecimal price, int quantity) {
        this.orderId = UUID.randomUUID().toString();
        this.instrumentId = instrumentId;
        this.traderId = traderId;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.status = OrderStatus.OPEN;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public String getTraderId() {
        return traderId;
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

    public void execute(int amount) {
        if (amount > quantity) {
            throw new IllegalArgumentException("Executed amount exceeds remaining quantity");
        }
        this.quantity -= amount;

        if (this.quantity == 0) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public boolean isFilled() {
        return this.status == OrderStatus.FILLED;
    }


    public enum OrderType {
        BUY,
        SELL
    }


    public enum OrderStatus {
        OPEN,
        PARTIALLY_FILLED,
        FILLED,
        CANCELLED
    }
}
