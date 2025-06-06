package com.example.tradingSystem.service;

import com.example.tradingSystem.model.Instrument;
import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TradingService logic.
 */
class TradingServiceImplTest {
    private TradingServiceImpl tradingServiceImpl;
    private Instrument instrument;

    @BeforeEach
    void setUp() {
        tradingServiceImpl = new TradingServiceImpl();
        instrument = new Instrument("AAPL");
        tradingServiceImpl.registerInstrument(instrument);
    }

    @Test
    void testRegisterInstrument() {
        assertTrue(tradingServiceImpl.getAllInstruments().contains(instrument));
    }

    @Test
    void testPlaceOrderAndMatch() {
        Order buyOrder = new Order("trader1", instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order("trader2", instrument.getId(), Order.OrderType.SELL, new BigDecimal("100"), 10);
        tradingServiceImpl.placeOrder(buyOrder);
        List<Trade> trades = tradingServiceImpl.placeOrder(sellOrder);
        assertEquals(1, trades.size());
        assertEquals(0, tradingServiceImpl.getOrderBook(instrument.getId()).getBuyOrders().size());
        assertEquals(0, tradingServiceImpl.getOrderBook(instrument.getId()).getSellOrders().size());
    }

    @Test
    void testCancelOrder() {
        Order buyOrder = new Order("trader1", instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        tradingServiceImpl.placeOrder(buyOrder);
        boolean cancelled = tradingServiceImpl.cancelOrder(instrument.getId(), buyOrder.getOrderId());
        assertTrue(cancelled);
        assertEquals(0, tradingServiceImpl.getOrderBook(instrument.getId()).getBuyOrders().size());
    }

    @Test
    void testGetMarketPrice() {
        // Add orders directly to the order book to control the matching
        var orderBook = tradingServiceImpl.getOrderBook(instrument.getId());
        Order buyOrder = new Order("trader1", instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order("trader2", instrument.getId(), Order.OrderType.SELL, new BigDecimal("100"), 10);
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        // Update market price after adding orders
        tradingServiceImpl.getAllInstruments().forEach(i -> {
            if (i.getId().equals(instrument.getId())) {
                // Forcibly update market price
                try {
                    var method = tradingServiceImpl.getClass().getDeclaredMethod("updateMarketPrice", String.class);
                    method.setAccessible(true);
                    method.invoke(tradingServiceImpl, instrument.getId());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // Check market price before matching
        BigDecimal marketPrice = tradingServiceImpl.getMarketPrice(instrument.getId());
        assertEquals(new BigDecimal("105"), marketPrice);
        // Now trigger matching and check that market price is 0 (no orders left)
        orderBook.matchOrders();
        tradingServiceImpl.getAllInstruments().forEach(i -> {
            if (i.getId().equals(instrument.getId())) {
                try {
                    var method = tradingServiceImpl.getClass().getDeclaredMethod("updateMarketPrice", String.class);
                    method.setAccessible(true);
                    method.invoke(tradingServiceImpl, instrument.getId());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        BigDecimal afterMatchPrice = tradingServiceImpl.getMarketPrice(instrument.getId());
        assertEquals(BigDecimal.ZERO, afterMatchPrice);
    }
} 