package com.example.trading_system.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for trading system errors.
 * This exception is used to handle business logic errors in the trading system.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
@Schema(description = "Error response for trading system operations")
public class TradingException extends RuntimeException {
    
    @Schema(description = "Error code that identifies the type of error", example = "INVALID_ORDER")
    private final String errorCode;
    
    @Schema(description = "Detailed error message explaining what went wrong", example = "Order price cannot be negative")
    private final String message;

    public TradingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Common error codes used in the trading system.
     */
    @Schema(description = "Error codes used in the trading system")
    public enum ErrorCode {
        @Schema(description = "Invalid order parameters (price, quantity, etc.)")
        INVALID_ORDER,
        
        @Schema(description = "Instrument not found in the system")
        INSTRUMENT_NOT_FOUND,
        
        @Schema(description = "Order not found or already filled/cancelled")
        ORDER_NOT_FOUND,
        
        @Schema(description = "Insufficient funds or quantity for the order")
        INSUFFICIENT_RESOURCES,
        
        @Schema(description = "System is currently unavailable")
        SYSTEM_ERROR,

        @Schema(description = "Order queue is full and cannot accept more orders")
        ORDER_QUEUE_FULL
    }
}
