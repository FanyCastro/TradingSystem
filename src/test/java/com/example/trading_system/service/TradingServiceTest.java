package com.example.trading_system.service;

import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TradingService logic.
 */
class TradingServiceTest {
    private TradingService tradingService;
    private Instrument instrument;

    @BeforeEach
    void setUp() {
        tradingService = new TradingService();
        instrument = new Instrument("AAPL");
        tradingService.registerInstrument(instrument);
    }

    @Test
    void testRegisterInstrument() {
        assertTrue(tradingService.getAllInstruments().contains(instrument));
    }

    @Test
    void testPlaceOrderAndMatch() {
        Order buyOrder = new Order("trader1", instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order("trader2", instrument.getId(), Order.OrderType.SELL, new BigDecimal("100"), 10);
        tradingService.placeOrder(buyOrder);
        List<Trade> trades = tradingService.placeOrder(sellOrder);
        assertEquals(1, trades.size());
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getBuyOrders().size());
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getSellOrders().size());
    }

    @Test
    void testCancelOrder() {
        Order buyOrder = new Order("trader1", instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        tradingService.placeOrder(buyOrder);
        boolean cancelled = tradingService.cancelOrder(instrument.getId(), buyOrder.getOrderId());
        assertTrue(cancelled);
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getBuyOrders().size());
    }

    @Test
    void testGetMarketPrice() {
        // Add orders directly to the order book to control the matching
        var orderBook = tradingService.getOrderBook(instrument.getId());
        Order buyOrder = new Order("trader1", instrument.getId(), Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order("trader2", instrument.getId(), Order.OrderType.SELL, new BigDecimal("100"), 10);
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
                    throw new RuntimeException(e);
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
                    throw new RuntimeException(e);
                }
            }
        });
        BigDecimal afterMatchPrice = tradingService.getMarketPrice(instrument.getId());
        assertEquals(BigDecimal.ZERO, afterMatchPrice);
    }
} 