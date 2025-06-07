package com.example.trading_system.service;

import com.example.trading_system.exception.TradingException;
import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TradingService orchestrates the trading logic for multiple instruments.
 * It manages order books, places/cancels orders, and provides market data.
 */
public class TradingServiceImpl implements TradingService {
    private static final Logger log = LoggerFactory.getLogger(TradingServiceImpl.class);

    // Map of traderId to its OrderBook
    private final Map<String, OrderBook> orderBooks;
    // Map of traderId to Instrument (for market price, etc.)
    private final Map<String, Instrument> instruments;

    public TradingServiceImpl() {
        log.info("Initializing TradingService");
        this.orderBooks = new HashMap<>();
        this.instruments = new HashMap<>();
    }

    public void registerInstrument(Instrument instrument) {
        log.info("Registering new instrument: {}", instrument.getSymbol());
        instruments.put(instrument.getId(), instrument);
        orderBooks.putIfAbsent(instrument.getId(), new InMemoryOrderBook(instrument.getId()));
        log.debug("Instrument {} registered with ID {}", instrument.getSymbol(), instrument.getId());
    }

    public List<Trade> placeOrder(Order order) {
        log.info("Placing order {} for instrument {}: type={}, price={}, quantity={}", 
            order.getOrderId(), order.getInstrumentId(), order.getType(), 
            order.getPrice(), order.getQuantity());

        String instrumentId = order.getInstrumentId();
        OrderBook orderBook = orderBooks.get(instrumentId);
        if (orderBook == null) {
            log.error("Instrument not found: {}", instrumentId);
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                "Instrument not found: " + order.getInstrumentId());
        }

        orderBook.addOrder(order);
        log.debug("Order {} added to order book", order.getOrderId());

        List<Trade> trades = orderBook.matchOrders();
        if (!trades.isEmpty()) {
            log.info("Executed {} trades for order {}", trades.size(), order.getOrderId());
            trades.forEach(trade -> 
                log.debug("Trade executed: {} - Price: {}, Quantity: {}", 
                    trade.tradeId(), trade.price(), trade.quantity()));
        } else {
            log.debug("No trades executed for order {}", order.getOrderId());
        }

        Optional<BigDecimal> marketPrice = orderBook.getMarketPrice();
        marketPrice.ifPresent(price -> {
            Instrument instrument = instruments.get(instrumentId);
            if (instrument != null) {
                instrument.setMarketPrice(price);
                log.debug("Updated market price for {} to {}", instrument.getSymbol(), price);
            }
        });

        return trades;
    }

    @Override
    public void cancelOrder(String instrumentId, String orderId) {
        String sanitizedOrderId = sanitizeLogData(orderId);
        String sanitizedInstrumentId = sanitizeLogData(instrumentId);
        log.info("Cancelling order {} for instrument {}", sanitizedOrderId, sanitizedInstrumentId);
        
        OrderBook orderBook = orderBooks.get(sanitizedInstrumentId);
        if (orderBook == null) {
            log.warn("Attempted to cancel order for non-existent instrument: {}", sanitizedInstrumentId);
            return;
        }

        orderBook.cancelOrder(sanitizedOrderId);
        log.debug("Order {} cancelled", sanitizedOrderId);

        Optional<BigDecimal> marketPrice = orderBook.getMarketPrice();
        marketPrice.ifPresent(price -> {
            Instrument instrument = instruments.get(sanitizedInstrumentId);
            if (instrument != null) {
                instrument.setMarketPrice(price);
                log.debug("Updated market price for {} to {} after cancellation", 
                    instrument.getSymbol(), price);
            }
        });
    }

    private String sanitizeLogData(String data) {
        return data.replaceAll("[\\n\\r\\t]", "_");
    }

    public BigDecimal getMarketPrice(String instrumentId) {
        log.debug("Getting market price for instrument {}", instrumentId);
        
        Instrument instrument = instruments.get(instrumentId);
        if (instrument == null) {
            log.error("Instrument not found: {}", instrumentId);
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                    "Instrument not found: " + instrumentId);
        }
        
        BigDecimal price = instrument.getMarketPrice();
        log.debug("Market price for {}: {}", instrument.getSymbol(), price);
        return price;
    }

    @Override
    public OrderBook getOrderBook(String instrumentId) {
        log.debug("Getting order book for instrument {}", instrumentId);
        
        OrderBook orderBook = orderBooks.get(instrumentId);
        if (orderBook == null) {
            log.error("Order book not found for instrument: {}", instrumentId);
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                    "Instrument not found: " + instrumentId);
        }

        return orderBook;
    }

    public Collection<Instrument> getAllInstruments() {
        Collection<Instrument> instruments = this.instruments.values();
        log.debug("Retrieved {} instruments", instruments.size());
        return instruments;
    }

    @Override
    public List<Order> getOrdersByTrader(String traderId) {
        log.info("Getting all orders for trader {}", traderId);
        
        List<Order> orders = getAllInstruments().stream()
            .map(instrument -> getOrderBook(instrument.getId()))
            .filter(Objects::nonNull)
            .flatMap(orderBook -> orderBook.getAllOrders().values().stream())
            .filter(order -> order.getTraderId().equals(traderId))
            .toList();
            
        log.debug("Found {} orders for trader {}", orders.size(), traderId);
        return orders;
    }
}
