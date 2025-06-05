package com.example.trading_system.dto;

import com.example.trading_system.model.Order;

/**
 * DTO for returning order status and information to the client.
 */
public record OrderResponse(
    /** Unique order identifier */
    String orderId,
    /** Status of the order */
    Order.OrderStatus status,
    /** Informational message */
    String message
) {}
