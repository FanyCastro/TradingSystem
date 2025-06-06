package com.example.tradingSystem.controller;

import com.example.tradingSystem.model.Instrument;
import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.service.TradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrdersControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradingService tradingService;

    private static final String TRADER_ID = "TRADER123";

    @Test
    void testGetOrdersByTrader_success() throws Exception {
        // First register an instrument
        String symbol = "BTC";
        tradingService.registerInstrument(new Instrument(symbol));
        String firstId = tradingService.getAllInstruments().stream()
                .filter(order -> order.getSymbol().equals(symbol))
                .map(Instrument::getId).findFirst().get();

        // Place multiple orders for the same trader
        Order order1 = new Order(firstId, TRADER_ID, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        Order order2 = new Order(firstId, TRADER_ID, Order.OrderType.SELL, BigDecimal.valueOf(200), 5);
        tradingService.placeOrder(order1);
        tradingService.placeOrder(order2);

        // Place an order for a different trader
        Order otherOrder = new Order(firstId, "OTHER_TRADER", Order.OrderType.BUY, BigDecimal.valueOf(150), 8);
        tradingService.placeOrder(otherOrder);

        mockMvc.perform(get("/api/trading/orders")
                .param("traderId", TRADER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].orderId", containsInAnyOrder(order1.getOrderId(), order2.getOrderId())))
                .andExpect(jsonPath("$[*].status", everyItem(is("OPEN"))));
    }

    @Test
    void testGetOrdersByTrader_missingTraderId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/trading/orders"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrdersByTrader_noOrders_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/trading/orders")
                .param("traderId", TRADER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 