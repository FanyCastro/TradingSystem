package com.example.tradingSystem.service;

import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderBook logic.
 */
class OrderBookTest {
    private OrderBook orderBook;
    private static final String INSTRUMENT_ID = "TEST";

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook(INSTRUMENT_ID);
    }

    @Test
    void testAddOrder() {
        Order buyOrder = new Order( INSTRUMENT_ID, Order.OrderType.BUY, new BigDecimal("100"), 10);
        orderBook.addOrder(buyOrder);
        assertEquals(1, orderBook.getBuyOrders().size());
        assertTrue(orderBook.getBuyOrders().contains(buyOrder));
    }

    @Test
    void testCancelOrder() {
        Order sellOrder = new Order(INSTRUMENT_ID, Order.OrderType.SELL, new BigDecimal("105"), 5);
        orderBook.addOrder(sellOrder);
        boolean cancelled = orderBook.cancelOrder(sellOrder.getOrderId());
        assertTrue(cancelled);
        assertEquals(0, orderBook.getSellOrders().size());
    }

    @Test
    void testMatchOrders_FullFill() {
        Order buyOrder = new Order(INSTRUMENT_ID, Order.OrderType.BUY, new BigDecimal("110"), 10);
        Order sellOrder = new Order(INSTRUMENT_ID, Order.OrderType.SELL, new BigDecimal("100"), 10);
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(1, trades.size());
        assertEquals(0, orderBook.getBuyOrders().size());
        assertEquals(0, orderBook.getSellOrders().size());
        assertEquals(10, trades.get(0).quantity());
        assertEquals(new BigDecimal("100"), trades.get(0).price());
    }

    @Test
    void testMatchOrders_PartialFill() {
        Order buyOrder = new Order(INSTRUMENT_ID, Order.OrderType.BUY, new BigDecimal("110"), 5);
        Order sellOrder = new Order(INSTRUMENT_ID, Order.OrderType.SELL, new BigDecimal("100"), 10);
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(1, trades.size());
        assertEquals(0, orderBook.getBuyOrders().size());
        assertEquals(1, orderBook.getSellOrders().size());
        assertEquals(5, trades.get(0).quantity());
        assertEquals(new BigDecimal("100"), trades.get(0).price());
    }

    @Test
    void testNoMatchWhenPricesDoNotCross() {
        Order buyOrder = new Order(INSTRUMENT_ID, Order.OrderType.BUY, new BigDecimal("90"), 10);
        Order sellOrder = new Order(INSTRUMENT_ID, Order.OrderType.SELL, new BigDecimal("100"), 10);
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(0, trades.size());
        assertEquals(1, orderBook.getBuyOrders().size());
        assertEquals(1, orderBook.getSellOrders().size());
    }
} 