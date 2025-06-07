package com.example.trading_system.service;

import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for TradingService logic.
 */
class TradingServiceImplTest {
    private TradingService tradingService = new TradingServiceImpl();
    private static Instrument instrument;

    @BeforeAll
    static void setUpAll() {
        instrument = new Instrument("AAPL");
    }

    @BeforeEach
    void setUp() {
        tradingService = new TradingServiceImpl();
    }

    @Test
    void testRegisterInstrument() {
        tradingService.registerInstrument(instrument);
        assertTrue(tradingService.getAllInstruments().contains(instrument));
    }

    @Test
    void testPlaceOrderAndMatch() {
        tradingService.registerInstrument(instrument);
        Order buyOrder = new Order(instrument.getId(), "trader1", Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order(instrument.getId(), "trader2", Order.OrderType.SELL, new BigDecimal("100"), 10);
        
        tradingService.placeOrder(buyOrder);
        
        List<Trade> trades = tradingService.placeOrder(sellOrder);
        assertEquals(1, trades.size());
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getBuyOrders().size());
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getSellOrders().size());
    }

    @Test
    void testCancelOrder() {
        tradingService.registerInstrument(instrument);
        Order buyOrder = new Order(instrument.getId(), "trader1", Order.OrderType.BUY, new BigDecimal("110"), 10);

        tradingService.placeOrder(buyOrder);

        tradingService.cancelOrder(instrument.getId(), buyOrder.getOrderId());
        assertEquals(0, tradingService.getOrderBook(instrument.getId()).getBuyOrders().size());
    }

    @Test
    void testGetMarketPrice_shouldReturnZeroAfterMatching() {
        tradingService.registerInstrument(instrument);
        // After matching - should return zero as all orders are filled
        Order sellOrder = new Order(instrument.getId(), "trader2", Order.OrderType.SELL, new BigDecimal("50"), 4);
        tradingService.placeOrder(sellOrder);
        Order buyOrder = new Order(instrument.getId(), "trader1", Order.OrderType.BUY, new BigDecimal("60"), 4);
        tradingService.placeOrder(buyOrder);
        assertEquals(BigDecimal.ZERO, tradingService.getMarketPrice(instrument.getId()));
    }

    @Test
    void testGetMarketPrice_shouldReturnZeroWhenNoOrders() {
        tradingService.registerInstrument(instrument);
        // No orders - should return zero
        // TODO - review this test
        assertEquals(BigDecimal.ZERO, tradingService.getMarketPrice(instrument.getId()));
    }

    @Test
    void testGetMarketPrice_shouldReturnZeroWithOnlySellOrder() {
        tradingService.registerInstrument(instrument);
        // Only buy order - should return 0 (as we don't have best sell price)
        Order sellOrder = new Order(instrument.getId(), "trader2", Order.OrderType.SELL, new BigDecimal("100"), 10);
        tradingService.placeOrder(sellOrder);
        assertEquals(BigDecimal.ZERO, tradingService.getMarketPrice(instrument.getId()));
    }

    @Test
    void testGetMarketPrice_ShouldReturnZeroWithOnlyBuyOrder() {
        tradingService.registerInstrument(instrument);
        // Only buy order - should return 0 (as we don't have best sell price)
        Order buyOrder = new Order(instrument.getId(), "trader1", Order.OrderType.BUY, new BigDecimal("110"), 10);
        tradingService.placeOrder(buyOrder);
        assertEquals(BigDecimal.ZERO, tradingService.getMarketPrice(instrument.getId()));
    }

    @Test
    void testGetMarketPrice_shouldReturnCalcWithSellAndBuyOrder() {
        tradingService.registerInstrument(instrument);
        // Both buy and sell orders - should return average
        Order sellOrder = new Order(instrument.getId(), "trader2", Order.OrderType.SELL, new BigDecimal("100"), 2);
        tradingService.placeOrder(sellOrder);
        Order buyOrder = new Order(instrument.getId(), "trader1", Order.OrderType.BUY, new BigDecimal("80"), 5);
        tradingService.placeOrder(buyOrder);

        assertEquals(new BigDecimal("90"), tradingService.getMarketPrice(instrument.getId()));
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