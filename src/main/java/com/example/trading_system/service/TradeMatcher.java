package com.example.trading_system.service;

import com.example.trading_system.model.Trade;

import java.util.List;

public interface TradeMatcher {
    List<Trade> match(OrderBook orderBook, String instrumentId);
}
