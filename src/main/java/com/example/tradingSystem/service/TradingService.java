package com.example.tradingSystem.service;

import com.example.tradingSystem.model.Instrument;
import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface TradingService {
    /**
     * Registers a new instrument in the system.
     * @param instrument The instrument to register.
     */
    void registerInstrument(Instrument instrument);

    /**
     * Places an order and triggers order matching.
     * @param order The order to place.
     * @return List of trades executed as a result of this order.
     */
    List<Trade> placeOrder(Order order);

    /**
     * Cancels an order by its ID and instrument.
     * @param instrumentId The instrument ID.
     * @param orderId The order ID to cancel.
     * @return true if cancelled, false otherwise.
     */
    boolean cancelOrder(String instrumentId, String orderId);

    /**
     * Gets the current market price for an instrument (mid price between best buy and sell).
     * @param instrumentId The instrument ID.
     * @return The market price, or null if not available.
     */
    BigDecimal getMarketPrice(String instrumentId);

    /**
     * Gets the order book for an instrument.
     * @param instrumentId The instrument ID.
     * @return The OrderBook, or null if not found.
     */
    OrderBookImpl getOrderBook(String instrumentId);

    /**
     * Gets all registered instruments.
     */
    Collection<Instrument> getAllInstruments();

    /**
     * Gets all orders placed by a trader.
     * @param traderId The trader ID.
     * @return List of orders placed by the trader.
     */
    List<Order> getOrdersByTrader(String traderId);
}
