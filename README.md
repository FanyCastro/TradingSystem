# Trading System

![Java CI](https://img.shields.io/github/actions/workflow/status/FanyCastro/TradingSystem/maven.yml?logo=github&label=Build)
![Coverage](https://img.shields.io/codecov/c/github/FanyCastro/TradingSystem/main?logo=codecov&label=Coverage&token=YOUR_CODECOV_TOKEN)
[![Java](https://img.shields.io/badge/Java-21-blue?logo=java)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
![License](https://img.shields.io/github/license/FanyCastro/productsAPI?color=blue)

## Description
A simplified trading system for a financial exchange, implemented in Java 21 and Spring Boot. The system supports buy and sell orders for financial instruments, with a matching engine and REST API.

## Features
- Register financial instruments (symbol, unique ID, market price)
- Place and cancel buy/sell orders
- Order matching engine (price-time priority)
- Market price based on mid price between best buy and best sell orders
- REST API for all core operations
- Unit and integration tests included
- Swagger/OpenAPI documentation
- RESTful API design following best practices

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

## API Endpoints

### Instruments
- **Register instrument:**
  ```http
  POST /api/trading/instrument
  Content-Type: application/json
  {
    "symbol": "TSLA"
  }
  ```
  Response: 201 Created with Location header and instrument details

- **Get all instruments:**
  ```http
  GET /api/trading/instruments
  ```
  Response: 200 OK with list of instruments including their current market prices

### Orders
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
  Response: 201 Created with Location header and order details

- **Cancel order:**
  ```http
  DELETE /api/trading/order/{instrumentId}/{orderId}
  ```
  Response: 200 OK with cancellation status

### Market Data
- **Get market price:**
  ```http
  GET /api/trading/market-price/{instrumentId}
  ```
  Response: 200 OK with current market price

- **Get order book:**
  ```http
  GET /api/trading/order-book/{instrumentId}
  ```
  Response: 200 OK with current buy and sell orders

## REST API Design
The API follows REST best practices:
- **Resource Creation:**
  - POST requests return 201 Created
  - Location header points to the new resource
  - Response body contains the created resource
- **Resource Retrieval:**
  - GET requests return 200 OK
  - Response body contains the requested resource(s)
- **Resource Deletion:**
  - DELETE requests return 200 OK
  - Response body contains operation status
- **Error Handling:**
  - 400 Bad Request for invalid input
  - 404 Not Found for missing resources
  - 500 Internal Server Error for system issues
  - Error responses include error code and message

## Testing & Coverage
- **Unit tests:** Cover order matching, order book logic, and service layer
- **Integration tests:** Cover REST API endpoints and end-to-end flows
- **Unified coverage:** Both unit and integration tests are executed together with `mvn test` or `mvn verify`
- **How to run all tests and generate coverage report:**
  ```bash
  ./mvnw clean verify
  # or
  ./mvnw test
  ```
- **View coverage report:**
  Open `target/site/jacoco/index.html` in your browser

## Technical Notes
- Uses Java 21 records for immutable data (e.g., `Trade`, DTOs)
- Market price is always the mid price between best buy and sell orders (if both exist)
- Clean separation of concerns: model, service, controller, config, dto
- No external database: all data is in-memory for simplicity
- No UI: focus on core backend logic and API
- RESTful API design following best practices:
  - POST for creation (201 Created with Location header)
  - GET for retrieval
  - DELETE for cancellation
  - Proper error handling with meaningful status codes
  - Consistent response formats

## How to Extend
- Add persistence (JPA, MongoDB, etc.)
- Add authentication/authorization
- Add more advanced order types (market, stop, etc.)
- Add more endpoints for trade history, statistics, etc.
- Add WebSocket support for real-time updates
- Add rate limiting and circuit breakers

## API Documentation (Swagger/OpenAPI)
- Automatic interactive API documentation is available thanks to [springdoc-openapi](https://springdoc.org/)
- After running the application, access the Swagger UI at:
  - http://localhost:8080/swagger-ui.html
  - or http://localhost:8080/swagger-ui/index.html
- You can explore and test all endpoints directly from the browser
- Each endpoint includes:
  - Request/response examples
  - Error scenarios
  - Schema definitions
  - Authentication requirements (if any)

---

**Author:** Estefanía Castro
