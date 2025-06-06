package com.example.tradingSystem.service;

import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;
import com.example.tradingSystem.exception.TradingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderBook logic.
 */
class OrderBookImplTest {
    private OrderBookImpl orderBook;
    private static final String INSTRUMENT_ID = "BTC";
    private static final String TRADER_1 = "TRADER1";
    private static final String TRADER_2 = "TRADER2";

    @BeforeEach
    void setUp() {
        orderBook = new OrderBookImpl(INSTRUMENT_ID);
    }

    @Test
    void testAddOrder_buyOrder() {
        Order order = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        orderBook.addOrder(order);
        assertEquals(1, orderBook.getBuyOrders().size());
        assertEquals(0, orderBook.getSellOrders().size());
        assertEquals(Order.OrderStatus.OPEN, order.getStatus());
    }

    @Test
    void testAddOrder_sellOrder() {
        Order order = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        orderBook.addOrder(order);
        assertEquals(0, orderBook.getBuyOrders().size());
        assertEquals(1, orderBook.getSellOrders().size());
        assertEquals(Order.OrderStatus.OPEN, order.getStatus());
    }

    @Test
    void testAddOrder_sameTraderBuyAndSell() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.SELL, BigDecimal.valueOf(90), 10);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertTrue(trades.isEmpty(), "Should not match orders from same trader");
    }

    @Test
    void testMatchOrders_perfectMatch() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(1, trades.size());
        assertEquals(10, trades.getFirst().quantity());
        assertEquals(BigDecimal.valueOf(100), trades.getFirst().price());
        assertEquals(Order.OrderStatus.FILLED, buyOrder.getStatus());
        assertEquals(Order.OrderStatus.FILLED, sellOrder.getStatus());
    }

    @Test
    void testMatchOrders_partialMatch() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 15);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(1, trades.size());
        assertEquals(10, trades.getFirst().quantity());
        assertEquals(5, buyOrder.getQuantity());
        assertEquals(0, sellOrder.getQuantity());
        assertEquals(Order.OrderStatus.OPEN, buyOrder.getStatus());
        assertEquals(Order.OrderStatus.FILLED, sellOrder.getStatus());
    }

    @Test
    void testMatchOrders_multipleMatches() {
        Order buyOrder1 = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 5);
        Order buyOrder2 = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 5);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(buyOrder2);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(2, trades.size());
        assertEquals(5, trades.get(0).quantity());
        assertEquals(5, trades.get(1).quantity());
        assertEquals(Order.OrderStatus.FILLED, buyOrder1.getStatus());
        assertEquals(Order.OrderStatus.FILLED, buyOrder2.getStatus());
        assertEquals(Order.OrderStatus.FILLED, sellOrder.getStatus());
    }

    @Test
    void testMatchOrders_pricePriority() {
        Order buyOrder1 = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 5);
        Order buyOrder2 = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(101), 5);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 5);
        
        orderBook.addOrder(buyOrder1);
        orderBook.addOrder(buyOrder2);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(1, trades.size());
        assertEquals(buyOrder2.getOrderId(), trades.getFirst().buyOrderId());
    }

    @Test
    void testMatchOrders_noMatch() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(90), 10);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertTrue(trades.isEmpty());
        assertEquals(Order.OrderStatus.OPEN, buyOrder.getStatus());
        assertEquals(Order.OrderStatus.OPEN, sellOrder.getStatus());
    }

    @Test
    void testCancelOrder() {
        Order order = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        orderBook.addOrder(order);
        
        assertTrue(orderBook.cancelOrder(order.getOrderId()));
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        assertTrue(orderBook.getBuyOrders().isEmpty());
        
        // Verify order is still in allOrders but not in active queues
        assertNotNull(orderBook.getAllOrders().get(order.getOrderId()));
    }

    @Test
    void testCancelOrder_nonExistentOrder() {
        TradingException exception = assertThrows(TradingException.class, () -> {
            orderBook.cancelOrder("non-existent-id");
        });
        assertEquals("Order not found: non-existent-id", exception.getMessage());
    }

    @Test
    void testCancelOrder_alreadyCancelled() {
        Order order = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        orderBook.addOrder(order);
        
        assertTrue(orderBook.cancelOrder(order.getOrderId()));
        assertFalse(orderBook.cancelOrder(order.getOrderId())); // Second cancellation should fail
    }

    @Test
    void testMatchOrders_withCancelledOrders() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        orderBook.cancelOrder(buyOrder.getOrderId());
        
        List<Trade> trades = orderBook.matchOrders();
        assertTrue(trades.isEmpty());
        assertEquals(Order.OrderStatus.CANCELLED, buyOrder.getStatus());
        assertEquals(Order.OrderStatus.OPEN, sellOrder.getStatus());
    }

    @Test
    void testMatchOrders_edgeCase_zeroQuantity() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(100), 0);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertTrue(trades.isEmpty());
    }

    @Test
    void testMatchOrders_edgeCase_negativePrice() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, BigDecimal.valueOf(-100), 10);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, BigDecimal.valueOf(100), 10);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertTrue(trades.isEmpty());
    }

    @Test
    void testMatchOrders_edgeCase_largeNumbers() {
        Order buyOrder = new Order(INSTRUMENT_ID, TRADER_1, Order.OrderType.BUY, 
            BigDecimal.valueOf(Long.MAX_VALUE), Integer.MAX_VALUE);
        Order sellOrder = new Order(INSTRUMENT_ID, TRADER_2, Order.OrderType.SELL, 
            BigDecimal.valueOf(Long.MAX_VALUE), Integer.MAX_VALUE);
        
        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);
        
        List<Trade> trades = orderBook.matchOrders();
        assertEquals(1, trades.size());
        assertEquals(Integer.MAX_VALUE, trades.get(0).quantity());
    }
} 