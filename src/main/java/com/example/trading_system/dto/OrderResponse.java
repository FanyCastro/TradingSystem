package com.example.trading_system.dto;

import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.util.List;

/**
 * DTO for returning order status and information to the client.
 */
public record OrderResponse(
    String orderId,
    Order.OrderStatus status,
    List<Trade> trades
) {}
