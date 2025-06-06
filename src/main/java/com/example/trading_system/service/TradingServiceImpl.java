package com.example.trading_system.service;

import com.example.trading_system.exception.TradingException;
import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TradingService orchestrates the trading logic for multiple instruments.
 * It manages order books, places/cancels orders, and provides market data.
 */
public class TradingServiceImpl implements TradingService {
    // Map of traderId to its OrderBook
    private final Map<String, OrderBookImpl> orderBooks;
    // Map of traderId to Instrument (for market price, etc.)
    private final Map<String, Instrument> instruments;

    public TradingServiceImpl() {
        this.orderBooks = new HashMap<>();
        this.instruments = new HashMap<>();
    }

    public void registerInstrument(Instrument instrument) {
        instruments.put(instrument.getId(), instrument);
        orderBooks.putIfAbsent(instrument.getId(), new OrderBookImpl(instrument.getId()));
    }

    public List<Trade> placeOrder(Order order) {
        OrderBookImpl orderBookImpl = orderBooks.get(order.getInstrumentId());
        // TODO - it shouldn't happen as we are checking the instrument id in the controller
        if (orderBookImpl == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                "Instrument not found: " + order.getInstrumentId());
        }
        orderBookImpl.addOrder(order);
        updateMarketPrice(order.getInstrumentId());
        List<Trade> trades = orderBookImpl.matchOrders();
        if (!trades.isEmpty()) {
            updateMarketPrice(order.getInstrumentId());
        }
        return trades;
    }

    public boolean cancelOrder(String instrumentId, String orderId) {
        OrderBookImpl orderBookImpl = orderBooks.get(instrumentId);
        if (orderBookImpl == null) return false;
        boolean result = orderBookImpl.cancelOrder(orderId);
        updateMarketPrice(instrumentId);
        return result;
    }

    public BigDecimal getMarketPrice(String instrumentId) {
        Instrument instrument = instruments.get(instrumentId);
        if (instrument == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                    "Instrument not found: " + instrumentId);
        }
        return instrument.getMarketPrice();
    }

    public OrderBookImpl getOrderBook(String instrumentId) {
        OrderBookImpl orderBook = orderBooks.get(instrumentId);
        if (orderBook == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                    "Instrument not found: " + instrumentId);
        }

        return orderBook;
    }

    /**
     * Updates the market price of an instrument based on the best buy and sell orders.
     * Market price = (best buy price + best sell price) / 2
     * If only one side exists, use that price. If neither, set to zero.
     */
    private void updateMarketPrice(String instrumentId) {
        OrderBookImpl orderBookImpl = orderBooks.get(instrumentId);
        Instrument instrument = instruments.get(instrumentId);
        if (orderBookImpl == null || instrument == null) return;
        Order bestBuy = orderBookImpl.getBestBuyOrder();
        Order bestSell = orderBookImpl.getBestSellOrder();
        BigDecimal marketPrice;
        if (bestBuy != null && bestSell != null) {
            marketPrice = bestBuy.getPrice().add(bestSell.getPrice()).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        } else if (bestBuy != null) {
            marketPrice = bestBuy.getPrice();
        } else if (bestSell != null) {
            marketPrice = bestSell.getPrice();
        } else {
            marketPrice = BigDecimal.ZERO;
        }
        instrument.setMarketPrice(marketPrice);
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
