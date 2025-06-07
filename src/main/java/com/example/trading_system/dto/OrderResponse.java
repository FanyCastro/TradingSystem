package com.example.trading_system.dto;

import com.example.trading_system.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response DTO for an order")
public record OrderResponse(
        @Schema(description = "Unique identifier for the order", example = "123e4567-e89b-12d3-a456-426614174001")
        String orderId,
        @Schema(description = "Identifier of the instrument this order is for", example = "123e4567-e89b-12d3-a456-426614174000")
        String instrumentId,
        @Schema(description = "Type of the order: BUY or SELL", example = "BUY")
        Order.OrderType type,
        @Schema(description = "Identifier of the trader placing the order", example = "123e4567-e89b-12d3-a456-426614174000")
        String traderId,
        @Schema(description = "Limit price for the order", example = "100.00")
        BigDecimal price,
        @Schema(description = "Quantity of the order", example = "10")
        int quantity
) {
    public static OrderResponse fromOrder(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getInstrumentId(),
                order.getType(),
                order.getTraderId(),
                order.getPrice(),
                order.getQuantity()
        );
    }
} 