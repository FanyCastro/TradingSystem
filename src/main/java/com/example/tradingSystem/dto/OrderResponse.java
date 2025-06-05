package com.example.tradingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.example.tradingSystem.model.Order;

/**
 * DTO for returning order status and information to the client.
 */
public record OrderResponse(
    @Schema(description = "Unique order identifier", example = "123e4567-e89b-12d3-a456-426614174001")
    String orderId,
    @Schema(description = "Status of the order", example = "OPEN")
    Order.OrderStatus status,
    @Schema(description = "Informational message", example = "Order placed. No trades executed yet.")
    String message
) {}
