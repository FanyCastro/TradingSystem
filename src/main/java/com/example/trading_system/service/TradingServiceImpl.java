package com.example.trading_system.service;

import com.example.trading_system.exception.TradingException;
import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TradingService orchestrates the trading logic for multiple instruments.
 * It manages order books, places/cancels orders, and provides market data.
 */
public class TradingServiceImpl implements TradingService {
    // Map of traderId to its OrderBook
    private final Map<String, OrderBook> orderBooks;
    // Map of traderId to Instrument (for market price, etc.)
    private final Map<String, Instrument> instruments;

    public TradingServiceImpl() {
        this.orderBooks = new HashMap<>();
        this.instruments = new HashMap<>();
    }

    public void registerInstrument(Instrument instrument) {
        instruments.put(instrument.getId(), instrument);
        orderBooks.putIfAbsent(instrument.getId(), new InMemoryOrderBook(instrument.getId()));
    }

    public List<Trade> placeOrder(Order order) {
        String instrumentId = order.getInstrumentId();
        OrderBook orderBook = orderBooks.get(instrumentId);
        if (orderBook == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                "Instrument not found: " + order.getInstrumentId());
        }

        orderBook.addOrder(order);
        List<Trade> trades = orderBook.matchOrders();

        Optional<BigDecimal> marketPrice = orderBook.getMarketPrice();
        marketPrice.ifPresent(price -> {
            Instrument instrument = instruments.get(instrumentId);
            if (instrument != null) {
                instrument.setMarketPrice(price);
            }
        });

        return trades;
    }

    @Override
    public void cancelOrder(String instrumentId, String orderId) {
        OrderBook orderBook = orderBooks.get(instrumentId);
        if (orderBook == null) return;
        orderBook.cancelOrder(orderId);
        Optional<BigDecimal> marketPrice = orderBook.getMarketPrice();
        marketPrice.ifPresent(price -> {
            Instrument instrument = instruments.get(instrumentId);
            if (instrument != null) {
                instrument.setMarketPrice(price);
            }
        });
    }

    public BigDecimal getMarketPrice(String instrumentId) {
        Instrument instrument = instruments.get(instrumentId);
        if (instrument == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                    "Instrument not found: " + instrumentId);
        }
        return instrument.getMarketPrice();
    }

    @Override
    public OrderBook getOrderBook(String instrumentId) {
        OrderBook orderBook = orderBooks.get(instrumentId);
        if (orderBook == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                    "Instrument not found: " + instrumentId);
        }

        return orderBook;
    }

    public Collection<Instrument> getAllInstruments() {
        return instruments.values();
    }

    @Override
    public List<Order> getOrdersByTrader(String traderId) {
        return getAllInstruments().stream()
            .map(instrument -> getOrderBook(instrument.getId()))
            .filter(Objects::nonNull)
            .flatMap(orderBook -> orderBook.getAllOrders().values().stream())
            .filter(order -> order.getTraderId().equals(traderId))
            .collect(Collectors.toList());
    }
}
