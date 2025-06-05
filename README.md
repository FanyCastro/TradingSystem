# Trading System

![License](https://img.shields.io/github/license/FanyCastro/productsAPI?color=blue)
[![Java](https://img.shields.io/badge/Java-21-blue?logo=java)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

## Description
A simplified trading system for a financial exchange, implemented in Java 21 and Spring Boot. The system supports buy and sell orders for financial instruments, with a matching engine and REST API.

## Features
- Register financial instruments (symbol, unique ID, market price)
- Place and cancel buy/sell orders
- Order matching engine (price-time priority)
- Market price based on mid price between best buy and best sell orders
- REST API for all core operations
- Unit and integration tests included

## Architecture
- **Spring Boot** (Java 21)
- **Domain model:** `Instrument`, `Order`, `Trade` (record)
- **Service layer:** `OrderBook`, `TradingService`
- **REST API:** `TradingController`
- **Configuration:** `config/TradingSystemConfig.java`
- **DTOs:** For request/response objects
- **Tests:** JUnit 5 (unit and integration)

## How to Run
1. **Build and test:**
   ```bash
   ./mvnw clean verify
   ```
2. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   The API will be available at `http://localhost:8080/api/trading`

## API Endpoints (examples)
- **Register instrument:**
  ```http
  POST /api/trading/instrument?symbol=TSLA
  ```
- **Place order:**
  ```http
  POST /api/trading/order
  Content-Type: application/json
  {
    "traderId": "trader1",
    "instrumentId": "<instrumentId>",
    "type": "BUY",
    "price": 100,
    "quantity": 10
  }
  ```
- **Cancel order:**
  ```http
  DELETE /api/trading/order/{instrumentId}/{orderId}
  ```
- **Get market price:**
  ```http
  GET /api/trading/market-price/{instrumentId}
  ```
- **Get order book:**
  ```http
  GET /api/trading/order-book/{instrumentId}
  ```

## Testing & Coverage
- **Unit tests:** Cover order matching, order book logic, and service layer.
- **Integration tests:** Cover REST API endpoints and end-to-end flows.
- **Unified coverage:** Both unit and integration tests are executed together with `mvn test` or `mvn verify`, and JaCoCo coverage reflects the combined execution of all tests.
- **How to run all tests and generate coverage report:**
  ```bash
  ./mvnw clean verify
  # or
  ./mvnw test
  ```
- **View coverage report:**
  Open `target/site/jacoco/index.html` in your browser to see the detailed coverage report for the whole codebase.

## Technical Notes
- Uses Java 21 records for immutable data (e.g., `Trade`, DTOs)
- Market price is always the mid price between best buy and sell orders (if both exist)
- Clean separation of concerns: model, service, controller, config, dto
- No external database: all data is in-memory for simplicity
- No UI: focus on core backend logic and API

## How to Extend
- Add persistence (JPA, MongoDB, etc.)
- Add authentication/authorization
- Add more advanced order types (market, stop, etc.)
- Add more endpoints for trade history, statistics, etc.

---

**Author:** Estefanía Castro
