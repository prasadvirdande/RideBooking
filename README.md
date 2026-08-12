# RideBooking
# 🚕 Ride Booking System

A **production-oriented ride booking backend** built using **Java, Spring Boot, Spring Cloud, PostgreSQL, Redis, Kafka, and Razorpay**.

The application follows a **microservices architecture** where authentication, users, drivers, rides, payments, and notifications are separated into independently deployable services.

The system is designed to handle important real-world ride-booking concerns such as **driver location tracking, ride assignment, concurrent ride acceptance, authentication, payment processing, payment webhooks, and asynchronous communication**.

---

## 🏗️ Architecture

```text
                         ┌──────────────────┐
                         │     Frontend     │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │    API Gateway   │
                         └────────┬─────────┘
                                  │
             ┌────────────────────┼────────────────────┐
             │                    │                    │
             ▼                    ▼                    ▼
      ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
      │ Auth Service│      │ User Service│      │Driver Service│
      └─────────────┘      └─────────────┘      └──────┬──────┘
                                                        │
                                                        ▼
                                                  ┌───────────┐
                                                  │   Redis   │
                                                  │ Geo Index │
                                                  └───────────┘

                         ┌──────────────────┐
                         │   Ride Service   │
                         └────────┬─────────┘
                                  │
                    ┌─────────────┼──────────────┐
                    │             │              │
                    ▼             ▼              ▼
              ┌──────────┐ ┌────────────┐ ┌──────────────┐
              │ Payment  │ │   Kafka    │ │ Notification │
              │ Service  │ │            │ │   Service    │
              └────┬─────┘ └────────────┘ └──────────────┘
                   │
                   ▼
             ┌────────────┐
             │  Razorpay  │
             └────────────┘
```

---

## 🚀 Main Features

### 🔐 Authentication & Authorization

* User and driver authentication
* JWT-based authentication
* Access-token based API authorization
* Spring Security integration
* Role-based access control
* Authentication handled independently through the Auth Service

---

### 👤 User Service

Responsible for managing passenger/user information.

Responsibilities include:

* User registration
* User profile management
* User information retrieval
* User-related ride information
* Communication with other services using OpenFeign

---

### 🚗 Driver Service

Responsible for driver management and real-time driver availability.

Features include:

* Driver registration
* Driver profile management
* Driver availability
* Driver location updates
* Nearby-driver discovery
* Driver ride acceptance

Driver locations are maintained using **Redis Geospatial commands**.

Example Redis operations:

```text
GEOADD
GEOSEARCH
```

This allows the system to efficiently find drivers near a passenger's pickup location.

---

### 🚕 Ride Service

The Ride Service is the core service of the application.

Responsibilities include:

* Ride creation
* Pickup and destination management
* Driver assignment
* Fare calculation
* Ride status management
* Ride acceptance
* OTP verification
* Ride completion
* Payment status integration

Example ride lifecycle:

```text
REQUESTED
    ↓
DRIVER_ASSIGNED
    ↓
ACCEPTED
    ↓
DRIVER_ARRIVED
    ↓
RIDE_STARTED
    ↓
COMPLETED
    ↓
PAYMENT_PENDING
    ↓
PAYMENT_COMPLETED
```

The ride is **not considered fully completed until the payment process succeeds**.

---

# 📍 Driver Location & Redis

Redis is used for storing driver geographical locations.

Instead of repeatedly querying the relational database for nearby drivers, driver coordinates are stored in a Redis GEO index.

Example:

```text
driver-location
```

A driver's longitude and latitude can be stored using Redis GEO commands.

Nearby-driver search can then be performed using:

```text
GEOSEARCH
```

This provides a much more efficient approach for location-based driver discovery.

---

# 💰 Fare Calculation

The Ride Service calculates the approximate distance between pickup and destination coordinates.

The system uses geographical coordinates:

```text
Pickup Latitude
Pickup Longitude

Destination Latitude
Destination Longitude
```

Distance is calculated and then used for fare calculation.

Conceptually:

```text
Fare = Base Fare + Distance × Per-KM Rate
```

The fare calculation is kept inside the Ride Service so that ride-related business logic remains centralized.

---

# 💳 Payment Integration

The application integrates with **Razorpay** for online payments.

Payment Service maintains information such as:

```text
paymentId
rideId
userId
driverId
amount
paymentStatus
paymentMethod
gatewayName
gatewayOrderId
gatewayPaymentId
paymentLink
```

### Payment Flow

```text
Ride Completed
      ↓
Payment Pending
      ↓
Create Razorpay Order
      ↓
User Completes Payment
      ↓
Razorpay
      ↓
Webhook
      ↓
Payment Service
      ↓
Verify Signature
      ↓
Payment Successful
      ↓
Update Ride Payment Status
```

The system uses Razorpay webhooks to receive payment status updates from the payment gateway.

---

# 🔔 Razorpay Webhooks

The Payment Service exposes a webhook endpoint for Razorpay events.

```http
POST /api/payment/webhook
```

The webhook handler:

1. Receives the Razorpay event.
2. Extracts the webhook payload.
3. Verifies the Razorpay webhook signature.
4. Determines the payment status.
5. Updates the payment record.
6. Communicates the successful payment state back to the Ride Service.

This prevents the application from relying solely on the frontend to determine whether payment succeeded.

---

# 🔄 Inter-Service Communication

The application uses **OpenFeign** for synchronous communication between microservices.

Example:

```text
Ride Service
     │
     │ Feign
     ▼
Payment Service
```

This allows services to communicate through strongly typed HTTP clients instead of manually constructing HTTP requests.

Example use cases:

* Ride Service → Driver Service
* Ride Service → User Service
* Payment Service → Ride Service

---

# ⚡ Kafka

Apache Kafka is used for **asynchronous event-driven communication**.

Instead of tightly coupling every operation through synchronous HTTP calls, important events can be published to Kafka.

Example:

```text
Ride Service
     │
     │ Ride Created
     ▼
    Kafka
     │
     ├──────────────► Notification Service
     │
     ├──────────────► Payment Service
     │
     └──────────────► Other Consumers
```

Potential events include:

```text
RIDE_CREATED
RIDE_ACCEPTED
RIDE_STARTED
RIDE_COMPLETED
PAYMENT_COMPLETED
DRIVER_ASSIGNED
```

This architecture allows additional consumers to be introduced without heavily modifying the Ride Service.

---

# 🔒 Concurrency Handling

Ride booking introduces real-world concurrency problems.

For example:

```text
Driver A
   │
   ├── Request 1 ──► Accept Ride #123
   │
   └── Request 2 ──► Accept Ride #123
```

Two requests could attempt to modify the same ride simultaneously.

The application addresses concurrency using database-level techniques such as:

### Pessimistic Locking

A database row can be locked while a ride is being accepted.

Conceptually:

```text
SELECT ... FOR UPDATE
```

This ensures that only one transaction can modify the locked ride at a time.

### Optimistic Locking

Entity versioning can also be used:

```text
@Version
private Long version;
```

If two transactions attempt to update the same entity simultaneously, the stale transaction can fail instead of silently overwriting another update.

This is particularly useful for preventing issues such as:

```text
Two drivers
     ↓
Accept same ride
     ↓
Only one should succeed
```

---

# 🗄️ Database

The application uses a relational database for persistent transactional data.

PostgreSQL/MySQL can be used depending on the environment.

Typical entities include:

```text
User
Driver
Ride
Payment
```

Relational storage is used for data that requires:

* Transactions
* Consistency
* Relationships
* Durable persistence
* Querying

Redis is used separately for high-speed geospatial/location operations.

---

# 🧩 Microservices

The project is divided into independent services:

```text
Auth Service
User Service
Driver Service
Ride Service
Payment Service
Notification Service
API Gateway
Config Server
Service Registry
```

Each service has a focused responsibility.

This separation allows individual services to be:

* Developed independently
* Deployed independently
* Scaled independently
* Maintained independently

---

# 🌐 API Gateway

The API Gateway acts as the single entry point for clients.

```text
Client
  ↓
API Gateway
  ↓
Microservices
```

Responsibilities include:

* Request routing
* Centralized entry point
* Authentication integration
* Service discovery integration
* Hiding internal service URLs from clients

---

# 🧭 Service Discovery

The project uses **Netflix Eureka** for service discovery.

Instead of hardcoding service locations:

```text
http://localhost:8081
http://localhost:8082
http://localhost:8083
```

services register themselves with Eureka.

```text
             Eureka
           /    |    \
          /     |     \
       Auth   Ride   Payment
```

Services can discover each other dynamically.

---

# 🏛️ Technology Stack

| Technology           | Purpose                              |
| -------------------- | ------------------------------------ |
| Java                 | Backend programming                  |
| Spring Boot          | Microservice development             |
| Spring Security      | Authentication & authorization       |
| JWT                  | Token-based authentication           |
| Spring Data JPA      | Database access                      |
| PostgreSQL / MySQL   | Persistent storage                   |
| Redis                | Caching & geospatial driver tracking |
| Apache Kafka         | Event-driven communication           |
| OpenFeign            | Synchronous service communication    |
| Eureka               | Service discovery                    |
| Spring Cloud Gateway | API Gateway                          |
| Razorpay             | Payment processing                   |
| Docker               | Containerization                     |
| Maven                | Build & dependency management        |
| Postman              | API testing                          |
| Ngrok                | Local webhook testing                |

---

# 🔐 Security

The application uses JWT-based authentication.

Typical request flow:

```text
Login
  ↓
Auth Service
  ↓
JWT Access Token
  ↓
Client
  ↓
Authorization: Bearer <token>
  ↓
API Gateway / Service
  ↓
Spring Security
  ↓
Authorized Request
```

Protected endpoints require a valid JWT.

Different roles such as:

```text
USER
DRIVER
```

can be used to control access to APIs.

---

# 🐳 Docker

Infrastructure components can be containerized using Docker.

Example infrastructure:

```text
PostgreSQL
Redis
Kafka
Zookeeper / Kafka KRaft
```

This provides a consistent development environment and makes the application easier to run across different machines.

---

# 📦 Project Structure

A simplified structure:

```text
RideBooking/
│
├── auth-service/
│
├── user-service/
│
├── driver-service/
│
├── ride-service/
│
├── payment-service/
│
├── notification-service/
│
├── api-gateway/
│
├── config-server/
│
├── service-registry/
│
└── docker-compose.yml
```

---

# 🔄 End-to-End Ride Flow

```text
1. User logs in
        ↓
2. Auth Service generates JWT
        ↓
3. User requests a ride
        ↓
4. Ride Service creates ride
        ↓
5. Driver Service searches Redis
   for nearby available drivers
        ↓
6. Driver receives ride request
        ↓
7. Driver accepts ride
        ↓
8. Ride Service updates ride status
        ↓
9. Driver reaches pickup location
        ↓
10. OTP verification
        ↓
11. Ride starts
        ↓
12. Ride completes
        ↓
13. Payment becomes pending
        ↓
14. Razorpay payment initiated
        ↓
15. User completes payment
        ↓
16. Razorpay webhook received
        ↓
17. Signature verified
        ↓
18. Payment marked successful
        ↓
19. Ride payment status updated
        ↓
20. Notification/event published
```

---

# 🧪 Testing

APIs can be tested using Postman.

Important scenarios include:

* User registration
* User login
* Driver registration
* Driver location update
* Nearby-driver search
* Ride creation
* Ride acceptance
* Concurrent ride acceptance
* Ride status transitions
* OTP verification
* Payment order creation
* Razorpay webhook handling
* Invalid webhook signature
* Unauthorized API access
* Expired/invalid JWT

---

# 🎯 Key Engineering Concepts Demonstrated

This project demonstrates practical backend engineering concepts including:

* Microservices architecture
* REST APIs
* JWT authentication
* Spring Security
* Database transactions
* Pessimistic locking
* Optimistic locking
* Concurrency control
* Redis Geospatial indexing
* Distributed service communication
* OpenFeign
* Kafka event-driven architecture
* Payment gateway integration
* Webhook processing
* Webhook signature verification
* API Gateway
* Service discovery
* Docker
* Database persistence
* Asynchronous processing

---

# 🚀 Future Improvements

Potential improvements include:

* Real-time driver tracking using WebSockets
* Dynamic/surge pricing
* Driver-rating system
* Ride cancellation policies
* Kafka-based notification architecture
* Distributed tracing with OpenTelemetry
* Centralized logging
* Prometheus + Grafana monitoring
* Circuit breakers using Resilience4j
* Rate limiting
* Redis-based distributed locks where appropriate
* CI/CD pipeline
* Kubernetes deployment
* Automated integration tests
* Idempotency for payment and ride APIs

---

# 👨‍💻 Project Goal

The goal of this project is to build a **scalable ride-booking backend** that demonstrates how real-world systems handle:

```text
Authentication
      +
Location
      +
Concurrency
      +
Distributed Systems
      +
Payments
      +
Event-Driven Architecture
      +
Caching
      +
Microservices
```

The project focuses not only on CRUD operations, but also on solving **real backend engineering problems** such as concurrent ride acceptance, real-time driver discovery, payment consistency, service-to-service communication, and asynchronous event processing.

---

## ⭐ Skills Demonstrated

**Java • Spring Boot • Spring Security • JWT • Spring Cloud • Microservices • PostgreSQL • Redis • Kafka • OpenFeign • Eureka • API Gateway • Razorpay • Docker • REST APIs • JPA/Hibernate • Concurrency • Distributed Systems**
