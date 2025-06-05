package com.example.trading_system.controller;

import com.example.trading_system.dto.OrderRequest;
import com.example.trading_system.dto.OrderResponse;
import com.example.trading_system.model.Instrument;
import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;
import com.example.trading_system.service.TradingService;
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

    /**
     * Registers a new instrument in the system.
     */
    @PostMapping("/instrument")
    public ResponseEntity<Instrument> registerInstrument(@RequestParam String symbol) {
        Instrument instrument = new Instrument(symbol);
        tradingService.registerInstrument(instrument);
        return ResponseEntity.ok(instrument);
    }

    /**
     * Places a new order and returns the result.
     */
    @PostMapping("/order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
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

    /**
     * Cancels an order by instrument and order ID.
     */
    @DeleteMapping("/order/{instrumentId}/{orderId}")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable String instrumentId, @PathVariable String orderId) {
        boolean cancelled = tradingService.cancelOrder(instrumentId, orderId);
        String message = cancelled ? "Order cancelled." : "Order not found or already filled/cancelled.";
        return ResponseEntity.ok(new CancelOrderResponse(cancelled, message));
    }

    /**
     * Gets the current market price for an instrument.
     */
    @GetMapping("/market-price/{instrumentId}")
    public ResponseEntity<MarketPriceResponse> getMarketPrice(@PathVariable String instrumentId) {
        BigDecimal price = tradingService.getMarketPrice(instrumentId);
        return ResponseEntity.ok(new MarketPriceResponse(price));
    }

    /**
     * Gets the order book for an instrument (buy and sell orders).
     */
    @GetMapping("/order-book/{instrumentId}")
    public ResponseEntity<OrderBookResponse> getOrderBook(@PathVariable String instrumentId) {
        var orderBook = tradingService.getOrderBook(instrumentId);
        if (orderBook == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new OrderBookResponse(orderBook.getBuyOrders(), orderBook.getSellOrders()));
    }

    /**
     * Gets all registered instruments.
     */
    @GetMapping("/instruments")
    public ResponseEntity<Collection<Instrument>> getAllInstruments() {
        return ResponseEntity.ok(tradingService.getAllInstruments());
    }

    /**
     * Simple record for cancel order response.
     */
    public record CancelOrderResponse(boolean cancelled, String message) {}

    /**
     * Simple record for market price response.
     */
    public record MarketPriceResponse(BigDecimal price) {}

    /**
     * Simple record for order book response.
     */
    public record OrderBookResponse(List<Order> buyOrders, List<Order> sellOrders) {}
}
