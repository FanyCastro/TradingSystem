package com.example.tradingSystem.config;

import com.example.tradingSystem.service.TradingService;
import com.example.tradingSystem.service.TradingServiceImpl;
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