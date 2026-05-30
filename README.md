# eStore

<img width="1440" height="774" alt="image" src="https://github.com/user-attachments/assets/78a6a2b7-5ef8-4701-9806-a253ed32298f" />

A full-stack e-commerce platform built with Kotlin, Spring Boot, PostgreSQL, React, and AWS infrastructure.

**TL;DR-** check out the project's live endpoints:  
Frontend: https://app.jacobwebstore.com  
API: https://api.jacobwebstore.com  
Swagger UI: https://api.jacobwebstore.com/swagger-ui.html  

---

## Screenshots

### Product Catalog

<img width="1440" height="778" alt="image" src="https://github.com/user-attachments/assets/a91dd136-b380-42f0-a409-519427791716" />

Browse products with pagination and filtering

### Shopping Cart

<img width="1440" height="776" alt="image" src="https://github.com/user-attachments/assets/f9a723de-e0f2-4d1a-b865-8dbe1f458c8f" />

Add products to a cart and review selections before checkout.

### Checkout

<img width="1440" height="773" alt="image" src="https://github.com/user-attachments/assets/ef5e9154-0130-4d4f-af5e-874440023904" />

<img width="1435" height="690" alt="image" src="https://github.com/user-attachments/assets/fc112455-33b5-4c21-be24-2c605a1f9093" />

Simulated payment processing and order placement.

### Swagger/OpenAPI Documentation

<img width="1440" height="777" alt="image" src="https://github.com/user-attachments/assets/947bd240-0214-42ae-9ffe-b0064d14e68c" />

Interactive API documentation for testing endpoints.

---

## Overview

eStore is a full-stack e-commerce platform built to explore backend architecture, authentication and authorization, cloud deployment, and production-style operational concepts.

Key capabilities include:

* JWT authentication and authorization
* Role-based access control
* Product management
* Shopping cart and checkout flows
* Order history
* Payment simulation
* Dockerized backend deployment
* HTTPS-secured infrastructure hosted on AWS

---
## Features

### Authentication & Security

* JWT authentication
* Role-based authorization (`ADMIN` / `USER`)
* Spring Security filter chain configuration
* Protected admin endpoints
* Password hashing with BCrypt

### Product Management

* Product CRUD operations
* Admin-only product creation and updates
* Pagination and filtering support

### Shopping Experience

* Shopping cart management
* Checkout flow
* Order history
* Payment simulation with validation

### Infrastructure & Deployment

* Dockerized backend deployment
* HTTPS-enabled frontend and backend
* Reverse proxy configuration using Nginx
* CDN/static hosting using AWS CloudFront + S3
* PostgreSQL persistence
* Flyway database migrations

---

## Tech Stack

### Backend

* Kotlin
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* Flyway
* Gradle

### Frontend

* React
* Vite
* Axios

### Infrastructure

* Docker
* Nginx
* AWS EC2
* AWS S3
* AWS CloudFront
* AWS ACM

---

## Architecture

The frontend is served through CloudFront and communicates with a Dockerized Spring Boot API hosted on EC2 behind an Nginx reverse proxy.

```text
React Frontend (S3 + CloudFront)
                ↓ HTTPS
        Nginx Reverse Proxy
                ↓
     Spring Boot API (Docker)
                ↓
           PostgreSQL

Authentication:
JWT → Spring Security → Role-Based Authorization
```

---

## Key Engineering Decisions

- JWT authentication was chosen to enable stateless authentication between the frontend and backend.
- PostgreSQL was selected for its reliability and strong transactional guarantees.
- Flyway was used to manage database schema changes across environments.
- Docker was used to ensure consistent deployments between local and production environments.
- CloudFront and S3 were chosen to provide cost-effective frontend hosting with HTTPS support.
- Checkout operations were designed to be idempotent to prevent duplicate purchases.

## Engineering Challenges & Lessons Learned

- Diagnosed a Spring Security filter-chain issue where JWT authentication was unintentionally bypassed for protected product endpoints.
- Resolved CORS issues between a CloudFront-hosted frontend and an EC2-hosted backend.
- Configured HTTPS using ACM certificates, Nginx, and custom domains.
- Debugged stale Docker image deployments caused by outdated container images.
- Configured CloudFront SPA routing to correctly serve frontend routes on page refresh.
- Implemented idempotent checkout behavior to prevent duplicate purchases during retries.

---

## API Documentation

Swagger/OpenAPI documentation is available at:

```text
https://api.jacobwebstore.com/swagger-ui.html
```

---

## Example API Requests

### Login

```bash
curl -X POST https://api.jacobwebstore.com/api/auth/login \
-H "Content-Type: application/json" \
-d '{
  "email": "admin@example.com",
  "password": "password"
}'
```

### Create Product (Admin)

```bash
curl -X POST https://api.jacobwebstore.com/api/products \
-H "Authorization: Bearer <TOKEN>" \
-H "Content-Type: application/json" \
-d '{
  "name": "Mechanical Keyboard",
  "description": "Hot-swappable mechanical keyboard",
  "price": 120,
  "stockQuantity": 25
}'
```

---

## Local Development

### Backend

```bash
./gradlew bootRun
```

Backend runs on:

```text
http://localhost:8081
```

### Frontend

```bash
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

---

## Deployment

The production environment is hosted on AWS using:

* EC2 for backend hosting
* Docker for containerization
* Nginx as a reverse proxy
* CloudFront + S3 for frontend hosting
* ACM-managed SSL certificates for HTTPS

---

## Future Improvements

* Refresh token support
* CI/CD pipeline
* Expanded integration testing
* Product image hosting
* Observability and metrics
* Payment provider integration
* Product image upload and storage

---
