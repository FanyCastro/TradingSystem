package com.example.trading_system.controller;

import com.example.trading_system.dto.InstrumentRequest;
import com.example.trading_system.dto.OrderRequest;
import com.example.trading_system.dto.InstrumentOrderResponse;
import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import com.example.trading_system.service.OrderBook;
import com.example.trading_system.service.TradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing trading system endpoints.
 */
@RestController
@RequestMapping("/api/trading/instruments")
@Tag(name = "Trading API", description = "API for managing trading instruments")
public class InstrumentsController {
    private final TradingService tradingService;

    public InstrumentsController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("")
    @Operation(summary = "Register a new trading instrument")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Instrument registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid instrument data")
    })
    public ResponseEntity<Instrument> registerInstrument(
            @Valid @RequestBody InstrumentRequest request) {
        Instrument instrument = new Instrument(request.symbol());
        tradingService.registerInstrument(instrument);
        return new ResponseEntity<>(instrument, HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Get all registered instruments")
    @ApiResponse(responseCode = "200", description = "List of instruments retrieved successfully")
    public ResponseEntity<Collection<Instrument>> getAllInstruments() {
        return ResponseEntity.ok(tradingService.getAllInstruments());
    }

    @PostMapping("/{id}/order")
    @Operation(summary = "Place a new order for an instrument")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order placed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid order data"),
        @ApiResponse(responseCode = "404", description = "Instrument not found")
    })
    public ResponseEntity<InstrumentOrderResponse> placeOrder(
            @Parameter(description = "ID of the instrument to place the order for") 
            @PathVariable String id,
            @Valid @RequestBody OrderRequest request) {
        Order order = new Order(id, request.traderId(), request.type(), request.price(), request.quantity());
        List<Trade> trades = tradingService.placeOrder(order);
        return new ResponseEntity<>(
            new InstrumentOrderResponse(order.getOrderId(), order.getStatus(), trades),
            HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{id}/orders/{orderId}")
    @Operation(summary = "Cancel an existing order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "ID of the instrument")
            @PathVariable String id,
            @Parameter(description = "ID of the order to cancel")
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9\\-]{1,36}$") String orderId) {
        tradingService.cancelOrder(id, orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/price")
    @Operation(summary = "Get the current market price for an instrument")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Price retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Instrument not found")
    })
    public ResponseEntity<BigDecimal> getMarketPrice(
            @Parameter(description = "ID of the instrument to get price for")
            @PathVariable String id) {
        return ResponseEntity.ok(tradingService.getMarketPrice(id));
    }

    @GetMapping("/{id}/orderbook")
    @Operation(summary = "Get the current order book for an instrument")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order book retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Instrument not found")
    })
    public ResponseEntity<Map<String, List<Order>>> getOrderBook(
            @Parameter(description = "ID of the instrument to get order book for")
            @PathVariable String id) {
        OrderBook orderBook = tradingService.getOrderBook(id);
        Map<String, List<Order>> response = new HashMap<>();
        response.put("buyOrders", orderBook.getBuyOrders());
        response.put("sellOrders", orderBook.getSellOrders());
        return ResponseEntity.ok(response);
    }
}
