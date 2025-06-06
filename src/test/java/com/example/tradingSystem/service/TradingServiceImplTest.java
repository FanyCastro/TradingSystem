package com.example.tradingSystem.service;

import com.example.tradingSystem.model.Instrument;
import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;
import com.example.tradingSystem.exception.TradingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TradingService logic.
 */
class tradingServiceTest {
    private TradingService tradingService;

    @BeforeEach
    void setUp() {
        tradingService = new TradingServiceImpl();
    }

    @Test
    void testRegisterInstrument() {
        Instrument instrument = new Instrument("AAPL");
        tradingService.registerInstrument(instrument);
        assertTrue(tradingService.getAllInstruments().contains(instrument));
    }

    @Test
    void testPlaceOrderAndMatch() {
        Instrument instrument = new Instrument("AAPL");
        tradingService.registerInstrument(instrument);
        Order buyOrder = new Order(instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order(instrument.getId(), Order.OrderType.SELL, new BigDecimal("100"), 10);
        
        tradingService.placeOrder(buyOrder);
        
        List<Trade> trades = tradingService.placeOrder(sellOrder);
        assertEquals(1, trades.size());
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getBuyOrders().size());
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getSellOrders().size());
    }

    @Test
    void testCancelOrder() {
        Instrument instrument = new Instrument("AAPL");
        tradingService.registerInstrument(instrument);
        Order buyOrder = new Order(instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);

        tradingService.placeOrder(buyOrder);

        boolean cancelled = tradingService.cancelOrder(instrument.getId(), buyOrder.getOrderId());
        assertTrue(cancelled);
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getBuyOrders().size());
    }

    @Test
    void testGetMarketPrice() {
        // Add orders directly to the order book to control the matching
        Instrument instrument = new Instrument("AAPL");
        tradingService.registerInstrument(instrument);
        var orderBook = tradingService.getOrderBook(instrument.getId());
        Order buyOrder = new Order(instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order(instrument.getId(), Order.OrderType.SELL, new BigDecimal("100"), 10);
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        // Update market price after adding orders
        tradingService.getAllInstruments().forEach(i -> {
            if (i.getId().equals(instrument.getId())) {
                // Forcibly update market price
                try {
                    var method = tradingService.getClass().getDeclaredMethod("updateMarketPrice", String.class);
                    method.setAccessible(true);
                    method.invoke(tradingService, instrument.getId());
                } catch (Exception e) {
                    throw new TradingException(TradingException.ErrorCode.SYSTEM_ERROR.name(),
                        "Failed to update market price: " + e.getMessage());
                }
            }
        });
        // Check market price before matching
        BigDecimal marketPrice = tradingService.getMarketPrice(instrument.getId());
        assertEquals(new BigDecimal("105"), marketPrice);
        // Now trigger matching and check that market price is 0 (no orders left)
        orderBook.matchOrders();
        tradingService.getAllInstruments().forEach(i -> {
            if (i.getId().equals(instrument.getId())) {
                try {
                    var method = tradingService.getClass().getDeclaredMethod("updateMarketPrice", String.class);
                    method.setAccessible(true);
                    method.invoke(tradingService, instrument.getId());
                } catch (Exception e) {
                    throw new TradingException(TradingException.ErrorCode.SYSTEM_ERROR.name(),
                        "Failed to update market price: " + e.getMessage());
                }
            }
        });
        BigDecimal afterMatchPrice = tradingService.getMarketPrice(instrument.getId());
        assertEquals(BigDecimal.ZERO, afterMatchPrice);
    }

    @Test
    void testGetAllInstruments() {
        // Register multiple instruments
        Instrument instrument1 = new Instrument("AAPL");
        Instrument instrument2 = new Instrument("GOOGL");
        Instrument instrument3 = new Instrument("MSFT");
        
        tradingService.registerInstrument(instrument1);
        tradingService.registerInstrument(instrument2);
        tradingService.registerInstrument(instrument3);
        
        Collection<Instrument> instruments = tradingService.getAllInstruments();
        assertEquals(3, instruments.size());
        assertTrue(instruments.contains(instrument1));
        assertTrue(instruments.contains(instrument2));
        assertTrue(instruments.contains(instrument3));
    }

    @Test
    void testGetAllInstruments_empty() {
        Collection<Instrument> instruments = tradingService.getAllInstruments();
        assertTrue(instruments.isEmpty());
    }
} 