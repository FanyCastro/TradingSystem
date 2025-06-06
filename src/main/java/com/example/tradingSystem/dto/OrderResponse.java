package com.example.tradingSystem.dto;

import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;

import java.util.List;

/**
 * DTO for returning order status and information to the client.
 */
public record OrderResponse(
    String orderId,
    Order.OrderStatus status,
    List<Trade> trades
) {}
