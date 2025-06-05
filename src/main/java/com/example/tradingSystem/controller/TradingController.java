package com.example.tradingSystem.controller;

import com.example.tradingSystem.dto.OrderRequest;
import com.example.tradingSystem.dto.OrderResponse;
import com.example.tradingSystem.exception.TradingException;
import com.example.tradingSystem.model.Instrument;
import com.example.tradingSystem.model.Order;
import com.example.tradingSystem.model.Trade;
import com.example.tradingSystem.service.TradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * REST controller exposing trading system endpoints.
 */
@RestController
@RequestMapping("/api/trading")
public class TradingController {
    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @Operation(summary = "Register a new instrument", description = "Registers a new financial instrument in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instrument registered successfully", 
            content = @Content(schema = @Schema(implementation = Instrument.class))),
        @ApiResponse(responseCode = "400", description = "Invalid symbol", 
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"INVALID_SYMBOL\", \"message\": \"Symbol cannot be empty or contain special characters\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"SYSTEM_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    @PostMapping("/instrument")
    public ResponseEntity<Instrument> registerInstrument(@RequestParam String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingException(TradingException.ErrorCode.INVALID_SYMBOL.name(), 
                "Symbol cannot be empty or contain special characters");
        }
        Instrument instrument = new Instrument(symbol);
        tradingService.registerInstrument(instrument);
        return ResponseEntity.ok(instrument);
    }

    @Operation(summary = "Place a new order", description = "Places a new buy or sell order for a given instrument.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order placed successfully", 
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid order request", 
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = {
                @ExampleObject(name = "Invalid Price", value = "{\"errorCode\": \"INVALID_ORDER\", \"message\": \"Order price cannot be negative\"}"),
                @ExampleObject(name = "Invalid Quantity", value = "{\"errorCode\": \"INVALID_ORDER\", \"message\": \"Order quantity must be greater than zero\"}")
            })),
        @ApiResponse(responseCode = "404", description = "Instrument not found",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"INSTRUMENT_NOT_FOUND\", \"message\": \"Instrument with ID 123 not found\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"SYSTEM_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    @PostMapping("/order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        validateOrderRequest(request);
        Order order = new Order(
                request.traderId(),
                request.instrumentId(),
                request.type(),
                request.price(),
                request.quantity()
        );
        List<Trade> trades = tradingService.placeOrder(order);
        String message = trades.isEmpty() ? "Order placed. No trades executed yet." : "Order matched and trades executed.";
        return ResponseEntity.ok(new OrderResponse(order.getOrderId(), order.getStatus(), message));
    }

    @Operation(summary = "Cancel an order", description = "Cancels an order by instrument and order ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled or not found", 
            content = @Content(schema = @Schema(implementation = CancelOrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"ORDER_NOT_FOUND\", \"message\": \"Order with ID 123 not found\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"SYSTEM_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    @DeleteMapping("/order/{instrumentId}/{orderId}")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable String instrumentId, @PathVariable String orderId) {
        boolean cancelled = tradingService.cancelOrder(instrumentId, orderId);
        String message = cancelled ? "Order cancelled." : "Order not found or already filled/cancelled.";
        return ResponseEntity.ok(new CancelOrderResponse(cancelled, message));
    }

    @Operation(summary = "Get market price", description = "Gets the current market price for an instrument (mid price between best buy and sell orders).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Market price returned", 
            content = @Content(schema = @Schema(implementation = MarketPriceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Instrument not found",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"INSTRUMENT_NOT_FOUND\", \"message\": \"Instrument with ID 123 not found\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"SYSTEM_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    @GetMapping("/market-price/{instrumentId}")
    public ResponseEntity<MarketPriceResponse> getMarketPrice(@PathVariable String instrumentId) {
        BigDecimal price = tradingService.getMarketPrice(instrumentId);
        if (price == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                "Instrument with ID " + instrumentId + " not found");
        }
        return ResponseEntity.ok(new MarketPriceResponse(price));
    }

    @Operation(summary = "Get order book", description = "Gets the order book for an instrument (buy and sell orders).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order book returned", 
            content = @Content(schema = @Schema(implementation = OrderBookResponse.class))),
        @ApiResponse(responseCode = "404", description = "Instrument not found",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"INSTRUMENT_NOT_FOUND\", \"message\": \"Instrument with ID 123 not found\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"SYSTEM_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    @GetMapping("/order-book/{instrumentId}")
    public ResponseEntity<OrderBookResponse> getOrderBook(@PathVariable String instrumentId) {
        var orderBook = tradingService.getOrderBook(instrumentId);
        if (orderBook == null) {
            throw new TradingException(TradingException.ErrorCode.INSTRUMENT_NOT_FOUND.name(),
                "Instrument with ID " + instrumentId + " not found");
        }
        return ResponseEntity.ok(new OrderBookResponse(orderBook.getBuyOrders(), orderBook.getSellOrders()));
    }

    @Operation(summary = "Get all instruments", description = "Gets all registered financial instruments.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of instruments returned", 
            content = @Content(schema = @Schema(implementation = Instrument.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = TradingException.class),
            examples = @ExampleObject(value = "{\"errorCode\": \"SYSTEM_ERROR\", \"message\": \"An unexpected error occurred\"}")))
    })
    @GetMapping("/instruments")
    public ResponseEntity<Collection<Instrument>> getAllInstruments() {
        return ResponseEntity.ok(tradingService.getAllInstruments());
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request.price() != null && request.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new TradingException(TradingException.ErrorCode.INVALID_ORDER.name(),
                "Order price cannot be negative");
        }
        if (request.quantity() <= 0) {
            throw new TradingException(TradingException.ErrorCode.INVALID_ORDER.name(),
                "Order quantity must be greater than zero");
        }
    }

    public record CancelOrderResponse(
            @Schema(description = "Whether the order was cancelled", example = "true") boolean cancelled,
            @Schema(description = "Informational message", example = "Order cancelled.") String message) {}

    public record MarketPriceResponse(
            @Schema(description = "Current market price", example = "105.00") BigDecimal price) {}

    public record OrderBookResponse(
            @Schema(description = "List of buy orders") List<Order> buyOrders,
            @Schema(description = "List of sell orders") List<Order> sellOrders) {}
}
