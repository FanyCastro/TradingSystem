package com.example.trading_system.dto;

import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO for returning order status and information to the client.
 */
@Schema(description = "DTO for returning order status and information to the client after placing an order")
public record InstrumentOrderResponse(
    @Schema(description = "Unique identifier for the order", example = "123e4567-e89b-12d3-a456-426614174001")
    String orderId,
    @Schema(description = "Current status of the order", example = "OPEN")
    Order.OrderStatus status,
    @Schema(description = "List of trades executed as a result of this order", example = "[]")
    List<Trade> trades
) {}
