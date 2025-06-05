package com.example.tradingSystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    void testRegisterInstrument_success() throws Exception {
        String symbol = "TSLA";
        mockMvc.perform(post("/api/trading/instrument")
                .param("symbol", symbol))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is(symbol)));
    }

    @Test
    void testRegisterInstrument_invalidSymbol_returnsTradingException() throws Exception {
        mockMvc.perform(post("/api/trading/instrument")
                .param("symbol", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("INVALID_SYMBOL")))
                .andExpect(jsonPath("$.message", containsString("Symbol cannot be empty")));
    }

    @Test
    void testPlaceOrder_success() throws Exception {
        // Register instrument
        String instrumentResponse = mockMvc.perform(post("/api/trading/instrument")
                .param("symbol", "AAPL"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String instrumentId = extractField(instrumentResponse, "id");
        // Place order
        String orderJson = "{" +
                "\"traderId\":\"trader1\"," +
                "\"instrumentId\":\"" + instrumentId + "\"," +
                "\"type\":\"BUY\"," +
                "\"price\":100," +
                "\"quantity\":10}";
        mockMvc.perform(post("/api/trading/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    void testPlaceOrder_invalidPrice_returnsTradingException() throws Exception {
        // Register instrument
        String instrumentResponse = mockMvc.perform(post("/api/trading/instrument")
                .param("symbol", "ERR"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String instrumentId = extractField(instrumentResponse, "id");
        // Place order with negative price
        String orderJson = "{" +
                "\"traderId\":\"trader1\"," +
                "\"instrumentId\":\"" + instrumentId + "\"," +
                "\"type\":\"BUY\"," +
                "\"price\":-10," +
                "\"quantity\":1}";
        mockMvc.perform(post("/api/trading/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("INVALID_ORDER")))
                .andExpect(jsonPath("$.message", containsString("price cannot be negative")));
    }

    @Test
    void testOrderBookAndCancelOrder_success() throws Exception {
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

    @Test
    void testGetMarketPrice_instrumentNotFound_returnsTradingException() throws Exception {
        mockMvc.perform(get("/api/trading/market-price/doesnotexist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("INSTRUMENT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void testRegisterInstrumentAndPlaceOrderAndQuery_success() throws Exception {
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
} 