package com.example.trading_system.dto;

import java.math.BigDecimal;
import com.example.trading_system.model.Order;

/**
 * DTO for placing a new order. Used as the request body in the REST API.
 */
public record OrderRequest(
    /** Trader placing the order */
    String traderId,
    /** Instrument to trade */
    String instrumentId,
    /** Order type: BUY or SELL */
    Order.OrderType type,
    /** Limit price (nullable for market orders) */
    BigDecimal price,
    /** Quantity to buy or sell */
    int quantity
) {}
