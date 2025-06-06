package com.example.trading_system.config;

import com.example.trading_system.service.TradingService;
import com.example.trading_system.service.TradingServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for trading system beans.
 * Defines beans for dependency injection.
 */
@Configuration
public class TradingSystemConfig {
    /**
     * Provides a singleton TradingService bean for dependency injection.
     */
    @Bean
    public TradingService tradingService() {
        return new TradingServiceImpl();
    }
} 