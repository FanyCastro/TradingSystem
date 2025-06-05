package com.example.trading_system.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a financial instrument (e.g., stock, crypto) that can be traded in the system.
 */
public class Instrument {
    /**
     * Unique identifier for the instrument.
     */
    private String id;

    /**
     * Symbol of the instrument (e.g., "AAPL", "BTC").
     */
    private String symbol;

    /**
     * Current market price, calculated as the mid price between best buy and sell orders.
     */
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
