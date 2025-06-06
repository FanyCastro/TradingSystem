package com.example.tradingSystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InstrumentRequest(
    @NotBlank(message = "Symbol is required")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Symbol must contain only uppercase letters and numbers")
    @Schema(description = "Symbol of the instrument", example = "BTC")
    String symbol
) {}
