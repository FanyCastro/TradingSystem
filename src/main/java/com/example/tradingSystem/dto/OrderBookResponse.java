package com.example.tradingSystem.dto;

import com.example.tradingSystem.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OrderBookResponse(
        @Schema(description = "List of buy orders")
        List<Order> buyOrders,
        @Schema(description = "List of sell orders")
        List<Order> sellOrders
) {}
