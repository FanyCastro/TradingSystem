package com.example.tradingSystem.controller;

import com.example.tradingSystem.model.Instrument;
import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.service.TradingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InstrumentsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradingService tradingService;

    private static final String TRADER_ID = "TRADER123";

    @Test
    void testRegisterInstrument_success() throws Exception {
        mockMvc.perform(post("/api/trading/instruments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symbol\": \"BTC\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.symbol", is("BTC")))
                .andExpect(jsonPath("$.marketPrice", is(0)));
    }

    @Test
    void testRegisterInstrument_invalidSymbol_returnsTradingException() throws Exception {
        // Test lowercase symbol
        mockMvc.perform(post("/api/trading/instruments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symbol\": \"btc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Symbol must contain only uppercase letters and numbers")));

        // Test symbol with special characters
        mockMvc.perform(post("/api/trading/instruments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symbol\": \"BTC-USD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Symbol must contain only uppercase letters and numbers")));

        // Test empty symbol
        mockMvc.perform(post("/api/trading/instruments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symbol\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Symbol is required")));
    }

    @Test
    void testPlaceOrder_success() throws Exception {
        // First register an instrument
        String symbol = "BTC";
        tradingService.registerInstrument(new Instrument(symbol));
        String firstId = tradingService.getAllInstruments().stream()
                .filter(order -> order.getSymbol().equals(symbol))
                .map(Instrument::getId).findFirst().get();
        tradingService.registerInstrument(new Instrument(firstId));

        mockMvc.perform(post("/api/trading/instruments/{instrumentId}/order", firstId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"BUY\", \"price\": 100.00, \"quantity\": 10, \"traderId\": \"" + TRADER_ID + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.trades", isA(List.class)));
    }

    @Test
    void testPlaceOrder_invalidInstrument_returnsTradingException() throws Exception {
        mockMvc.perform(post(
                "/api/trading/instruments/{instrumentId}/order", "NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"BUY\", \"price\": 100.00, \"quantity\": 10, \"traderId\": \"" + TRADER_ID + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("INSTRUMENT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Instrument not found")));
    }

    @Test
    void testPlaceOrder_invalidPrice_returnsTradingException() throws Exception {
        mockMvc.perform(post(
                "/api/trading/instruments/{instrumentId}/order", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"BUY\", \"price\": -100.00, \"quantity\": 10, \"traderId\": \"" + TRADER_ID + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Price must be greater than 0")));
    }

    @Test
    void testPlaceOrder_missingTraderId_returnsValidationError() throws Exception {
        mockMvc.perform(post(
                "/api/trading/instruments/{instrumentId}/order", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\": \"BUY\", \"price\": 100.00, \"quantity\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Trader ID is required")));
    }

    @Test
    void testCancelOrder_success() throws Exception {
        // First register an instrument and place an order
        String symbol = "BTC";
        tradingService.registerInstrument(new Instrument(symbol));
        String firstId = tradingService.getAllInstruments().stream()
                .filter(order -> order.getSymbol().equals(symbol))
                .map(Instrument::getId).findFirst().get();
        Order order = new Order(firstId, TRADER_ID, Order.OrderType.BUY, BigDecimal.valueOf(100), 10);
        tradingService.placeOrder(order);

        mockMvc.perform(delete("/api/trading/instruments/{id}/orders/{orderId}",
                        firstId, order.getOrderId()))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelOrder_nonexistentOrder_returnsTradingException() throws Exception {
        // First register an instrument
        String symbol = "BTC";
        tradingService.registerInstrument(new Instrument(symbol));
        String firstId = tradingService.getAllInstruments().stream()
                .filter(order -> order.getSymbol().equals(symbol))
                .map(Instrument::getId).findFirst().get();
        tradingService.registerInstrument(new Instrument(firstId));

        mockMvc.perform(delete("/api/trading/instruments/{instrumentId}/orders/{orderId}", 
                firstId, UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("ORDER_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Order not found")));
    }

    @Test
    void testGetMarketPrice_success() throws Exception {
        // First register an instrument
        String symbol = "BTC";
        tradingService.registerInstrument(new Instrument(symbol));
        String firstId = tradingService.getAllInstruments().stream()
                .filter(order -> order.getSymbol().equals(symbol))
                .map(Instrument::getId).findFirst().get();
        tradingService.registerInstrument(new Instrument(firstId));

        mockMvc.perform(get("/api/trading/instruments/{instrumentId}/price", firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(0)));
    }

    @Test
    void testGetMarketPrice_nonexistentInstrument_returnsTradingException() throws Exception {
        mockMvc.perform(get("/api/trading/instruments/{instrumentId}/price", "NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("INSTRUMENT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Instrument not found")));
    }

    @Test
    void testGetOrderBook_success() throws Exception {
        // First register an instrument
        String symbol = "BTC";
        tradingService.registerInstrument(new Instrument(symbol));
        String firstId = tradingService.getAllInstruments().stream()
                .filter(order -> order.getSymbol().equals(symbol))
                .map(Instrument::getId).findFirst().get();
        tradingService.registerInstrument(new Instrument(firstId));

        mockMvc.perform(get("/api/trading/instruments/{instrumentId}/orderbook", firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyOrders", isA(List.class)))
                .andExpect(jsonPath("$.sellOrders", isA(List.class)));
    }

    @Test
    void testGetOrderBook_nonexistentInstrument_returnsTradingException() throws Exception {
        mockMvc.perform(get("/api/trading/instruments/{instrumentId}/orderbook", "NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("INSTRUMENT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Instrument not found")));
    }

    @Test
    void testGetAllInstruments_success() throws Exception {
        // Register multiple instruments
        tradingService.registerInstrument(new Instrument("BTC"));
        tradingService.registerInstrument(new Instrument("ETH"));
        tradingService.registerInstrument(new Instrument("XRP"));

        mockMvc.perform(get("/api/trading/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].symbol", containsInAnyOrder("BTC", "ETH", "XRP")));
    }
} 