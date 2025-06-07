package com.example.trading_system.service;

import java.math.BigDecimal;
import java.util.Optional;

public interface MarketPriceCalculator {
    Optional<BigDecimal> calculateMarketPrice(OrderBook orderBook);
}
