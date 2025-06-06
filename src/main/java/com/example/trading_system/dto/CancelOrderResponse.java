package com.example.trading_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CancelOrderResponse(
    @Schema(description = "Whether the order was cancelled", example = "true")
    boolean cancelled,
    @Schema(description = "Informational message", example = "Order cancelled.")
    String message
) {}
