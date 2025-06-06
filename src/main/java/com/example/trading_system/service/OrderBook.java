package com.example.trading_system.service;

import com.example.trading_system.model.Order;
import com.example.trading_system.model.Trade;

import java.util.List;

public interface OrderBook {
    /**
     * Adds an order to the order book.
     * @param order The order to add.
     */
    void addOrder(Order order);

    /**
     * Cancels an order by its ID.
     * @param orderId The ID of the order to cancel.
     * @return true if the order was found and cancelled, false otherwise.
     */
    boolean cancelOrder(String orderId);

    /**
     * Matches buy and sell orders and executes trades at the best available price.
     * Trades are executed when the best buy price >= best sell price.
     * @return List of executed trades.
     */
    List<Trade> matchOrders();
}