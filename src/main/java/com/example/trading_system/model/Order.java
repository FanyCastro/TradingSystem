package com.example.trading_system.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a buy or sell order placed by a trader.
 */
public class Order {
    @Schema(description = "Unique identifier for the order", example = "123e4567-e89b-12d3-a456-426614174001")
    private final String orderId;

    @Schema(description = "Identifier of the instrument this order is for", example = "123e4567-e89b-12d3-a456-426614174000")
    private final String instrumentId;

    @Schema(description = "Type of the order: BUY or SELL", example = "BUY")
    private final OrderType type;

    @Schema(description = "Identifier of the trader placing the order", example = "123e4567-e89b-12d3-a456-426614174000")
    private final String traderId;

    @Schema(description = "Limit price for the order (can be null for market orders)", example = "100.00")
    private final BigDecimal price;

    @Schema(description = "Quantity to buy or sell", example = "10")
    private int quantity;

    @Schema(description = "Current status of the order", example = "OPEN")
    private OrderStatus status;

    @Schema(description = "Timestamp when the order was created (used for FIFO priority)", example = "2024-06-05T21:00:00")
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

    @Schema(description = "Order type: BUY or SELL")
    public enum OrderType {
        BUY,
        SELL
    }

    @Schema(description = "Order status")
    public enum OrderStatus {
        OPEN,
        PARTIALLY_FILLED,
        FILLED,
        CANCELLED
    }
}
