package com.example.trading_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a trade executed when a buy and sell order are matched.
 * This record is immutable and uses Java 21 features.
 */
public record Trade(
    /** Unique identifier for the trade */
    String tradeId,
    /** ID of the buy order involved in the trade */
    String buyOrderId,
    /** ID of the sell order involved in the trade */
    String sellOrderId,
    /** ID of the instrument traded */
    String instrumentId,
    /** Price at which the trade was executed */
    BigDecimal price,
    /** Quantity traded */
    int quantity,
    /** Timestamp when the trade was executed */
    LocalDateTime timestamp
) {}
