package com.example.trading_system.service;

import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.math.BigDecimal;
import java.util.*;

/**
 * TradingService orchestrates the trading logic for multiple instruments.
 * It manages order books, places/cancels orders, and provides market data.
 */
public class TradingService {
    // Map of instrumentId to its OrderBook
    private final Map<String, OrderBook> orderBooks;
    // Map of instrumentId to Instrument (for market price, etc.)
    private final Map<String, Instrument> instruments;

    public TradingService() {
        this.orderBooks = new HashMap<>();
        this.instruments = new HashMap<>();
    }

    /**
     * Registers a new instrument in the system.
     * @param instrument The instrument to register.
     */
    public void registerInstrument(Instrument instrument) {
        instruments.put(instrument.getId(), instrument);
        orderBooks.putIfAbsent(instrument.getId(), new OrderBook(instrument.getId()));
    }

    /**
     * Places an order and triggers order matching.
     * @param order The order to place.
     * @return List of trades executed as a result of this order.
     */
    public List<Trade> placeOrder(Order order) {
        OrderBook orderBook = orderBooks.get(order.getInstrumentId());
        if (orderBook == null) {
            throw new IllegalArgumentException("Instrument not found: " + order.getInstrumentId());
        }
        orderBook.addOrder(order);
        updateMarketPrice(order.getInstrumentId());
        List<Trade> trades = orderBook.matchOrders();
        if (!trades.isEmpty()) {
            updateMarketPrice(order.getInstrumentId());
        }
        return trades;
    }

    /**
     * Cancels an order by its ID and instrument.
     * @param instrumentId The instrument ID.
     * @param orderId The order ID to cancel.
     * @return true if cancelled, false otherwise.
     */
    public boolean cancelOrder(String instrumentId, String orderId) {
        OrderBook orderBook = orderBooks.get(instrumentId);
        if (orderBook == null) return false;
        boolean result = orderBook.cancelOrder(orderId);
        updateMarketPrice(instrumentId);
        return result;
    }

    /**
     * Gets the current market price for an instrument (mid price between best buy and sell).
     * @param instrumentId The instrument ID.
     * @return The market price, or null if not available.
     */
    public BigDecimal getMarketPrice(String instrumentId) {
        Instrument instrument = instruments.get(instrumentId);
        if (instrument == null) return null;
        return instrument.getMarketPrice();
    }

    /**
     * Gets the order book for an instrument.
     * @param instrumentId The instrument ID.
     * @return The OrderBook, or null if not found.
     */
    public OrderBook getOrderBook(String instrumentId) {
        return orderBooks.get(instrumentId);
    }

    /**
     * Updates the market price of an instrument based on the best buy and sell orders.
     * Market price = (best buy price + best sell price) / 2
     * If only one side exists, use that price. If neither, set to zero.
     */
    private void updateMarketPrice(String instrumentId) {
        OrderBook orderBook = orderBooks.get(instrumentId);
        Instrument instrument = instruments.get(instrumentId);
        if (orderBook == null || instrument == null) return;
        Order bestBuy = orderBook.getBestBuyOrder();
        Order bestSell = orderBook.getBestSellOrder();
        BigDecimal marketPrice;
        if (bestBuy != null && bestSell != null) {
            marketPrice = bestBuy.getPrice().add(bestSell.getPrice()).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP);
        } else if (bestBuy != null) {
            marketPrice = bestBuy.getPrice();
        } else if (bestSell != null) {
            marketPrice = bestSell.getPrice();
        } else {
            marketPrice = BigDecimal.ZERO;
        }
        instrument.setMarketPrice(marketPrice);
    }

    /**
     * Gets all registered instruments.
     */
    public Collection<Instrument> getAllInstruments() {
        return instruments.values();
    }
}
