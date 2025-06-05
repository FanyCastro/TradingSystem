package com.example.trading_system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for TradingController REST API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TradingControllerIT {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String extractField(String json, String field) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get(field).asText();
    }

    @Test
    void testRegisterInstrumentAndPlaceOrderAndQuery() throws Exception {
        // Register a new instrument
        String symbol = "TSLA";
        String instrumentResponse = mockMvc.perform(post("/api/trading/instrument")
                .param("symbol", symbol))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is(symbol)))
                .andReturn().getResponse().getContentAsString();
        String instrumentId = extractField(instrumentResponse, "id");

        // Place a buy order
        String buyOrderJson = "{" +
                "\"traderId\":\"trader1\"," +
                "\"instrumentId\":\"" + instrumentId + "\"," +
                "\"type\":\"BUY\"," +
                "\"price\":110," +
                "\"quantity\":10}";
        mockMvc.perform(post("/api/trading/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buyOrderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPEN")));

        // Place a sell order
        String sellOrderJson = "{" +
                "\"traderId\":\"trader2\"," +
                "\"instrumentId\":\"" + instrumentId + "\"," +
                "\"type\":\"SELL\"," +
                "\"price\":100," +
                "\"quantity\":10}";
        mockMvc.perform(post("/api/trading/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sellOrderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", anyOf(is("FILLED"), is("PARTIALLY_FILLED"), is("OPEN"))));

        // Query market price (should be 0 after matching)
        mockMvc.perform(get("/api/trading/market-price/" + instrumentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(0)));
    }

    @Test
    void testOrderBookAndCancelOrder() throws Exception {
        // Register a new instrument
        String symbol = "AMZN";
        String instrumentResponse = mockMvc.perform(post("/api/trading/instrument")
                        .param("symbol", symbol))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String instrumentId = extractField(instrumentResponse, "id");

        // Place a buy order
        String buyOrderJson = "{" +
                "\"traderId\":\"trader1\"," +
                "\"instrumentId\":\"" + instrumentId + "\"," +
                "\"type\":\"BUY\"," +
                "\"price\":90," +
                "\"quantity\":5}";
        String buyOrderResponse = mockMvc.perform(post("/api/trading/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyOrderJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String buyOrderId = extractField(buyOrderResponse, "orderId");

        // Query order book (should have one buy order)
        mockMvc.perform(get("/api/trading/order-book/" + instrumentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyOrders", hasSize(1)))
                .andExpect(jsonPath("$.sellOrders", hasSize(0)));

        // Cancel the buy order
        mockMvc.perform(delete("/api/trading/order/" + instrumentId + "/" + buyOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled", is(true)));

        // Query order book again (should be empty)
        mockMvc.perform(get("/api/trading/order-book/" + instrumentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyOrders", hasSize(0)))
                .andExpect(jsonPath("$.sellOrders", hasSize(0)));
    }
} 