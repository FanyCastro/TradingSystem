package com.example.tradingSystem.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a financial instrument (e.g., stock, crypto) that can be traded in the system.
 */
public class Instrument {
    @Schema(description = "Unique identifier for the instrument", example = "123e4567-e89b-12d3-a456-426614174000")
    private final String id;

    @Schema(description = "Symbol of the instrument", example = "BTC")
    private final String symbol;

    @Schema(description = "Current market price, calculated as the mid price between best buy and sell orders", example = "105.50")
    private BigDecimal marketPrice;

    public Instrument(String symbol) {
        this.id = UUID.randomUUID().toString();
        this.symbol = symbol;
        this.marketPrice = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }
}
