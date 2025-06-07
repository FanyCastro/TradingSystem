package com.example.trading_system.service;

import com.example.trading_system.model.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class MidPriceCalculator implements MarketPriceCalculator{
    @Override
    public Optional<BigDecimal> calculateMarketPrice(OrderBook orderBook) {
        Optional<Order> bestBuyOrder = orderBook.getBestBuyOrder();
        Optional<Order> bestSellOrder = orderBook.getBestSellOrder();

        if (!bestBuyOrder.isPresent() || !bestSellOrder.isPresent()) {
            return Optional.of(BigDecimal.ZERO);
        }

        BigDecimal buyPrice = bestBuyOrder.get().getPrice();
        BigDecimal sellPrice = bestSellOrder.get().getPrice();
        BigDecimal marketPrice = buyPrice.add(sellPrice).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        return Optional.of(marketPrice);
    }
}
