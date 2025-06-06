package com.example.trading_system.dto;

import com.example.trading_system.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for placing a new order. Used as the request body in the REST API.
 */
public record OrderRequest(
    @NotBlank(message = "Trader ID is required")
    @Schema(description = "Trader that creates the order", example = "Trader 1")
    String traderId,

    @NotNull(message = "Order type is required")
    @Schema(description = "Order type: BUY or SELL", example = "BUY")
    Order.OrderType type,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Limit price (nullable for market orders)", example = "100.00")
    BigDecimal price,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity to buy or sell", example = "10")
    int quantity
) {}
