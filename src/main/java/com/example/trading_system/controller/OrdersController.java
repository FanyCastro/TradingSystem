package com.example.trading_system.controller;

import com.example.trading_system.model.Order;
import com.example.trading_system.service.TradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trading/orders")
@Tag(name = "Order API", description = "API for managing trading orders")
public class OrdersController {
    private final TradingService tradingService;

    public OrdersController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @GetMapping("")
    @Operation(summary = "Get all orders for a specific trader")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Trader ID is required")
    })
    public ResponseEntity<List<Order>> getOrdersByTrader(
            @Parameter(description = "ID of the trader to get orders for", required = true)
            @RequestParam(required = true) String traderId) {
        return ResponseEntity.ok(tradingService.getOrdersByTrader(traderId));
    }
} 