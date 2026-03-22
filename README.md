# eCommerce Store Backend

This is an eCommerce backend built with Kotlin and Spring Boot.

## Key Features
- JWT Authentication and Authorization using Spring Security
- A Product, Cart, and Order management system
- Simulated payment processing with persistence
- Idempotent checkout
- Order lifecycle management (`Pending payment -> Fulfilled`)
- Payment lifecycle tracking (`Pending -> Succeeded/Failed`)
- Dockerization with PostgreSQL
- Spring Boot Actuator health checks
- Correlation ID request tracing
- Swagger/OpenAPI documentation

---

## Tech Stack
- **Backend:** Kotlin, Spring Boot 4
- **Security:** Spring Security & JWT
- **Database:** PostgreSQL
- **ORM:** JPA / Hibernate
- **Build Tool:** Gradle
- **Containerization:** Docker + Docker Compose
- **Testing:** JUnit 5, Mockito

---

## Architecture Overview

The application follows a layered architecture:
**Controller -> Service -> Repository -> Database**

- **Controllers** handle HTTP requests
- **Services** contain business logic (checkout, payments, etc.)
- **Repositories** manage persistence via JPA
- **Entities** model domain objects (Order, Payment, User, etc.)

---

## Running the Application

### Option 1 - Docker (Recommended)

Start the entire system (app + database):

```bash
docker compose up --build
```
Once the containers are running:
- **API:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Health Check:** http://localhost:8081/actuator/health

### Option 2 - Local Development
Ensure PostgreSQL is running, then execute:
```bash
./gradlew bootrun
```

## Example Flow
### Checkout Flow
1. A user adds items to their cart
2. The user submits a checkout request
3. The system:
   - Validates the cart & stock
   - Creates an order (PENDING_PAYMENT)
   - Processes payment
   - Updates the order to PAID
   - Clears the cart
4. If the same request is retried, an existing order is returned

## Testing
### Run all tests:
```bash
./gradlew test
```

Includes:
- Unit tests for services
- Integration tests for checkout scenarios:
    - Successful checkout
    - Checking out with an empty cart (failure)
    - Checking out when there's insufficient stock (failure)
    - Idempotent request handling

## Future Improvements
- Flyway database migrations
- Redis caching / distributed idempotency
- Pagination & filtering for large datasets
- Rate limiting
- External payment gateway integration
- Cloud deployment

## Author - Jacob Kelley
