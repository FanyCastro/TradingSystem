package com.example.trading_system.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a trade executed when a buy and sell order are matched.
 * This record is immutable and uses Java 21 features.
 */
public record Trade(
    @Schema(description = "Unique identifier for the trade", example = "123e4567-e89b-12d3-a456-426614174002")
    String tradeId,
    @Schema(description = "ID of the buy order involved in the trade", example = "123e4567-e89b-12d3-a456-426614174001")
    String buyOrderId,
    @Schema(description = "ID of the sell order involved in the trade", example = "123e4567-e89b-12d3-a456-426614174003")
    String sellOrderId,
    @Schema(description = "ID of the instrument traded", example = "123e4567-e89b-12d3-a456-426614174000")
    String instrumentId,
    @Schema(description = "Price at which the trade was executed", example = "105.00")
    BigDecimal price,
    @Schema(description = "Quantity traded", example = "10")
    int quantity,
    @Schema(description = "Timestamp when the trade was executed", example = "2024-06-05T21:00:00")
    LocalDateTime timestamp
) {}
