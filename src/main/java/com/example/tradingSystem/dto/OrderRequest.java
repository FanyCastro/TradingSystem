package com.example.tradingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import com.example.tradingSystem.model.Order;

/**
 * DTO for placing a new order. Used as the request body in the REST API.
 */
public record OrderRequest(
    @Schema(description = "Trader placing the order", example = "trader1")
    String traderId,
    @Schema(description = "Instrument to trade", example = "123e4567-e89b-12d3-a456-426614174000")
    String instrumentId,
    @Schema(description = "Order type: BUY or SELL", example = "BUY")
    Order.OrderType type,
    @Schema(description = "Limit price (nullable for market orders)", example = "100.00")
    BigDecimal price,
    @Schema(description = "Quantity to buy or sell", example = "10")
    int quantity
) {}
