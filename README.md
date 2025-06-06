# Trading System

## Business Description

This system implements a trading engine that allows users to buy and sell financial instruments. The system is designed to be fast, reliable, and scalable, following financial market best practices.

### What does this system do?

1. **Instrument Management**
   - Allows registration of new financial instruments (stocks, bonds, etc.)
   - Each instrument has a unique identifier and market price
   - Prices are automatically updated based on trading activity

2. **Order Trading**
   - Users can place buy and sell orders
   - Orders are automatically executed when there's a match
   - Price-time priority system to ensure a fair market

3. **Market Transparency**
   - Users can view the order book in real-time
   - Access to updated market prices
   - History of executed trades

### Why is it important?

- **Market Efficiency**: The system ensures orders are executed at the best available price
- **Transparency**: All participants have access to the same information
- **Reliability**: Designed to handle large volumes of orders
- **Scalability**: Easy to extend to support new types of instruments and orders

## Technical Architecture

### Why Spring Boot?

Spring Boot was chosen for several reasons:
- **Productivity**: Rapid development with auto-configuration
- **Ecosystem**: Large number of libraries and tools
- **Maintainability**: Clean and well-structured code
- **Testing**: Excellent support for unit and integration testing

### Core Components

1. **TradingService**
   - Central service that orchestrates all trading operations
   - Maintains a map of instruments and their order books
   - Handles order placement, cancellation, and matching

2. **OrderBook**
   - Manages buy and sell orders for a specific instrument
   - Maintains separate sorted lists for buy and sell orders
   - Implements price-time priority matching algorithm

3. **Order**
   - Represents a trading order with:
     - Unique order ID
     - Instrument ID
     - Type (BUY/SELL)
     - Price
     - Quantity
     - Status (OPEN/FILLED/CANCELLED)
     - Timestamp

4. **Trade**
   - Created when orders are matched
   - Contains:
     - Unique trade ID
     - Buy order ID
     - Sell order ID
     - Price
     - Quantity
     - Timestamp

### How does order matching work?

The system implements a price-time based matching algorithm:

1. **Price Priority**
   - Buy orders: highest price first
   - Sell orders: lowest price first
   - This ensures traders get the best possible price

2. **Time Priority**
   - For orders at the same price, first in, first out
   - Implemented using precise timestamps
   - Ensures a fair and transparent market

3. **Matching Process**
   ```java
   // Pseudocode of the process
   when new order arrives:
     if order is BUY:
       look for best SELL order (lowest price)
       if price matches:
         execute trade
         update remaining quantities
     if order is SELL:
       look for best BUY order (highest price)
       if price matches:
         execute trade
         update remaining quantities
   ```

### Order Lifecycle

1. **Order Creation**
   - Generate unique order ID
   - Set initial status as OPEN
   - Add to appropriate order book

2. **Order Matching**
   - Check for potential matches
   - Create trades for matched quantities
   - Update order status if fully filled

3. **Order Cancellation**
   - Remove from order book
   - Update status to CANCELLED
   - No trades are created

### How is data handled in memory?

The system uses optimized data structures:

1. **OrderBook**
   - `TreeMap` for buy and sell orders
   - Sorted by price and timestamp
   - Efficient O(log n) search
   - Example:
   ```java
   private final TreeMap<BigDecimal, List<Order>> buyOrders;
   private final TreeMap<BigDecimal, List<Order>> sellOrders;
   ```

2. **Instruments**
   - `HashMap` for quick OrderBook access
   - Key: instrumentId
   - Value: corresponding OrderBook
   - Example:
   ```java
   private final Map<String, OrderBook> orderBooks;
   ```

3. **Trades**
   - List of executed trades
   - Maintained for audit and history

### Market Price Calculation

- Market price = (best buy price + best sell price) / 2
- If only one side exists, use that price
- If neither side exists, price is zero

### How are errors and validations handled?

1. **Input Validation**
   - Validation annotations in DTOs
   - Business validations in service layer
   - Descriptive error messages

2. **Error Handling**
   - Custom exceptions by error type
   - Appropriate HTTP responses
   - Detailed logging for debugging

The system uses a consistent error response format:
```json
{
  "errorCode": "ERROR_CODE",
  "message": "Detailed error message"
}
```

Common error codes:
- `VALIDATION_ERROR`: Input validation failed
- `INVALID_SYMBOL`: Invalid instrument symbol
- `INVALID_ORDER`: Invalid order parameters
- `INSTRUMENT_NOT_FOUND`: Instrument does not exist
- `SYSTEM_ERROR`: Unexpected system error

### How is the system tested?

1. **Unit Tests**
   - Matching logic
   - Business validations
   - Error handling

2. **Integration Tests**
   - Complete trading flows
   - Endpoint validation
   - Error scenarios

3. **Test Coverage**
   - Minimum 80% coverage
   - Focus on critical business logic
   - Automated tests in CI/CD

## API Endpoints

### Register Instrument
- **POST** `/api/trading/instrument`
- **Request Body**: 
  ```json
  {
    "symbol": "BTC"
  }
  ```
- **Response**: 201 Created
  ```json
  {
    "id": "BTC",
    "symbol": "BTC",
    "marketPrice": 0.00
  }
  ```
- **Validation**: 
  - Symbol must be uppercase letters and numbers only
  - Symbol is required
  - Symbol must be unique

### Place Order
- **POST** `/api/trading/instrument/{instrumentId}/order`
- **Request Body**: 
  ```json
  {
    "type": "BUY",
    "price": 100.00,
    "quantity": 10
  }
  ```
- **Response**: 201 Created
  ```json
  {
    "orderId": "order-123",
    "status": "OPEN",
    "trades": [
      {
        "tradeId": "trade-456",
        "price": 100.00,
        "quantity": 5
      }
    ]
  }
  ```
- **Validation**:
  - Price must be greater than 0
  - Quantity must be positive
  - Instrument must exist
  - Order type must be valid

### Cancel Order
- **DELETE** `/api/trading/instrument/{instrumentId}/order/{orderId}`
- **Response**: 200 OK
  ```json
  {
    "success": true,
    "message": "Order cancelled successfully"
  }
  ```

### Get Market Price
- **GET** `/api/trading/instrument/{instrumentId}/price`
- **Response**: 200 OK
  ```json
  {
    "instrumentId": "BTC",
    "price": 100.00,
    "timestamp": "2024-03-20T10:30:00Z"
  }
  ```

### Get Order Book
- **GET** `/api/trading/instrument/{instrumentId}/orderbook`
- **Response**: 200 OK
  ```json
  {
    "instrumentId": "BTC",
    "buyOrders": [
      {
        "orderId": "order-123",
        "price": 100.00,
        "quantity": 10,
        "timestamp": "2024-03-20T10:30:00Z"
      }
    ],
    "sellOrders": [
      {
        "orderId": "order-124",
        "price": 101.00,
        "quantity": 5,
        "timestamp": "2024-03-20T10:30:01Z"
      }
    ]
  }
  ```

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.8 or higher
- Git

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/trading-system.git
   cd trading-system
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## Testing & Coverage

### Running Tests

1. **Unit Tests**
   ```bash
   mvn test
   ```

2. **Integration Tests**
   ```bash
   mvn verify
   ```

3. **Test Coverage Report**
   ```bash
   mvn test jacoco:report
   ```
   The coverage report will be available at `target/site/jacoco/index.html`

### Test Structure

1. **Unit Tests**
   - `TradingServiceImplTest`: Core trading logic
   - `OrderBookTest`: Order matching and management
   - `OrderTest`: Order lifecycle and validation
   - `TradeTest`: Trade creation and properties

2. **Integration Tests**
   - `TradingControllerIT`: API endpoint testing
   - `TradingServiceIT`: End-to-end trading flows
   - `ErrorHandlingIT`: Error scenarios and responses

### Coverage Requirements

- Minimum 80% line coverage
- 100% coverage for critical business logic:
  - Order matching algorithm
  - Price calculation
  - Error handling
  - Input validation

## API Documentation

### Swagger UI

The API documentation is available through Swagger UI:
- URL: `http://localhost:8080/swagger-ui.html`
- Interactive documentation
- Try-it-out functionality
- Request/response examples

### OpenAPI Specification

The OpenAPI specification is available at:
- URL: `http://localhost:8080/v3/api-docs`
- Format: JSON
- Version: 3.0

### API Endpoints

#### Register Instrument
- **POST** `/api/trading/instrument`
- **Request Body**: 
  ```json
  {
    "symbol": "BTC"
  }
  ```
- **Response**: 201 Created
  ```json
  {
    "id": "BTC",
    "symbol": "BTC",
    "marketPrice": 0.00
  }
  ```
- **Validation**: 
  - Symbol must be uppercase letters and numbers only
  - Symbol is required
  - Symbol must be unique

#### Place Order
- **POST** `/api/trading/instrument/{instrumentId}/order`
- **Request Body**: 
  ```json
  {
    "type": "BUY",
    "price": 100.00,
    "quantity": 10
  }
  ```
- **Response**: 201 Created
  ```json
  {
    "orderId": "order-123",
    "status": "OPEN",
    "trades": [
      {
        "tradeId": "trade-456",
        "price": 100.00,
        "quantity": 5
      }
    ]
  }
  ```
- **Validation**:
  - Price must be greater than 0
  - Quantity must be positive
  - Instrument must exist
  - Order type must be valid

#### Cancel Order
- **DELETE** `/api/trading/instrument/{instrumentId}/order/{orderId}`
- **Response**: 200 OK
  ```json
  {
    "success": true,
    "message": "Order cancelled successfully"
  }
  ```

#### Get Market Price
- **GET** `/api/trading/instrument/{instrumentId}/price`
- **Response**: 200 OK
  ```json
  {
    "instrumentId": "BTC",
    "price": 100.00,
    "timestamp": "2024-03-20T10:30:00Z"
  }
  ```

#### Get Order Book
- **GET** `/api/trading/instrument/{instrumentId}/orderbook`
- **Response**: 200 OK
  ```json
  {
    "instrumentId": "BTC",
    "buyOrders": [
      {
        "orderId": "order-123",
        "price": 100.00,
        "quantity": 10,
        "timestamp": "2024-03-20T10:30:00Z"
      }
    ],
    "sellOrders": [
      {
        "orderId": "order-124",
        "price": 101.00,
        "quantity": 5,
        "timestamp": "2024-03-20T10:30:01Z"
      }
    ]
  }
  ```

### Error Responses

All error responses follow this format:
```json
{
  "errorCode": "ERROR_CODE",
  "message": "Detailed error message"
}
```

Common error codes:
- `VALIDATION_ERROR`: Input validation failed
- `INVALID_SYMBOL`: Invalid instrument symbol
- `INVALID_ORDER`: Invalid order parameters
- `INSTRUMENT_NOT_FOUND`: Instrument does not exist
- `SYSTEM_ERROR`: Unexpected system error

## Next Steps

- Data persistence
- User authentication
- Real-time updates
- Advanced order types
- Transaction history
- Risk management
- Performance monitoring

---

**Author:** Estefanía Castro
