package com.example.trading_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record MarketPriceResponse(
        @Schema(description = "Current market price", example = "105.00")
        BigDecimal price
) {}
