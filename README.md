# 🚕 Ride Booking Application

A **microservices-based Ride Booking Application** built using **Java, Spring Boot, Spring Cloud, PostgreSQL, Redis, Kafka, JWT, and Razorpay**.

The application is designed to handle the complete ride lifecycle, from user authentication and driver location tracking to ride booking, driver acceptance, OTP-based ride start, ride completion, and online payment processing.

---

## 🏗️ Architecture

The application is divided into multiple independent microservices.

```text
                        ┌─────────────────┐
                        │      Client     │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │   API Gateway   │
                        └────────┬────────┘
                                 │
             ┌───────────────────┼───────────────────┐
             │                   │                   │
             ▼                   ▼                   ▼
      ┌─────────────┐     ┌─────────────┐     ┌──────────────┐
      │Auth Service │     │User Service │     │Driver Service│
      └─────────────┘     └─────────────┘     └──────┬───────┘
                                                      │
                                                      ▼
                                                ┌───────────┐
                                                │   Redis   │
                                                │ GEO Index │
                                                └───────────┘

                         ┌────────────────┐
                         │  Ride Service  │
                         └───────┬────────┘
                                 │
                                 ▼
                         ┌────────────────┐
                         │ Payment Service│
                         └───────┬────────┘
                                 │
                                 ▼
                           ┌──────────┐
                           │ Razorpay │
                           └──────────┘

                         ┌────────────────┐
                         │     Kafka      │
                         └────────────────┘
```

---

# 📦 Microservices

The project currently contains the following services:

```text
Auth Service
User Service
Driver Service
Ride Service
Payment Service
API Gateway
Service Registry
```

---

# 🔐 Auth Service

The Auth Service handles authentication and authorization.

### Implemented

* User authentication
* Driver authentication
* JWT-based authentication
* Access token generation
* Spring Security integration
* Protected APIs
* Role-based authorization

The JWT is sent with requests using:

```http
Authorization: Bearer <token>
```

The protected services validate the JWT before allowing access to secured endpoints.

---

# 👤 User Service

The User Service manages user-related functionality.

### Responsibilities

* User registration
* User information
* User-related operations
* Communication with other services through Feign

The User Service is separated from authentication so that authentication responsibilities remain inside the Auth Service.

---

# 🚗 Driver Service

The Driver Service manages driver-related functionality.

### Implemented

* Driver registration
* Driver information
* Driver availability
* Driver location updates
* Driver location storage using Redis
* Searching for nearby drivers
* Driver ride acceptance

Driver locations are maintained using Redis Geospatial functionality.

---

# 📍 Redis Geospatial Driver Tracking

Redis is used to store the geographical location of available drivers.

The project uses a Redis GEO index for driver locations.

Example Redis key:

```text
driver-location
```

Driver coordinates are stored using Redis geospatial commands.

The application uses operations such as:

```text
GEOADD
GEOSEARCH
ZRANGE
```

This allows the Ride Booking system to find drivers based on their geographical distance from the pickup location.

Example:

```text
Passenger
    │
    │ Pickup Location
    ▼
Ride Service
    │
    ▼
Driver Service
    │
    ▼
Redis GEO
    │
    ▼
Nearby Drivers
```

---

# 🚕 Ride Service

The Ride Service is the main business service responsible for managing rides.

### Implemented

* Ride creation
* Pickup location
* Destination location
* Distance calculation
* Fare calculation
* Driver assignment
* Driver acceptance
* Ride status management
* OTP verification before ride start
* Ride completion
* Payment status handling

---

# 📏 Distance Calculation

The Ride Service calculates the distance between:

```text
Pickup Location
        ↓
Destination Location
```

using latitude and longitude coordinates.

The calculated distance is then used for fare calculation.

---

# 💰 Fare Calculation

The Ride Service calculates the ride fare based on the distance.

The basic flow is:

```text
Pickup Coordinates
        +
Destination Coordinates
        ↓
Distance Calculation
        ↓
Fare Calculation
        ↓
Ride Fare
```

The fare is stored as part of the ride information and is later used by the Payment Service.

---

# 🔑 OTP Verification

OTP verification has been added to prevent a ride from being started without passenger verification.

The flow is:

```text
Driver reaches pickup
        ↓
OTP verification
        ↓
OTP is valid
        ↓
Ride starts
```

The ride cannot proceed to the ride-start stage without successful OTP verification.

---

# 🔄 Ride Status

The Ride Service maintains the state of the ride throughout its lifecycle.

One of the important additions is:

```text
PAYMENT_PENDING
```

The ride is not treated as completely finished until the payment has been successfully processed.

The flow is:

```text
Ride Started
      ↓
Ride Completed
      ↓
PAYMENT_PENDING
      ↓
Payment Successful
      ↓
Final Payment Completion
```

---

# 💳 Payment Service

A separate Payment Service has been implemented for handling ride payments.

The payment entity contains information such as:

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

This keeps payment-related responsibilities separate from the Ride Service.

---

# 💰 Razorpay Integration

The application integrates **Razorpay** as the payment gateway.

The Payment Service provides an API to create a Razorpay order.

Example:

```http
POST /api/create-order
```

The backend creates the Razorpay order and returns the required payment information to the client.

---

# 🔔 Razorpay Webhook

A Razorpay webhook has also been implemented.

Endpoint:

```http
POST /api/payment/webhook
```

The webhook is used to receive payment events from Razorpay.

The flow is:

```text
User completes payment
        ↓
Razorpay
        ↓
Webhook
        ↓
Payment Service
        ↓
Verify webhook signature
        ↓
Update payment status
        ↓
Update ride payment status
```

The webhook signature is verified before accepting the payment event.

This prevents unauthorized requests from being treated as valid Razorpay payment notifications.

---

# 🔗 Payment → Ride Service Communication

The Payment Service communicates with the Ride Service using a Feign Client.

After receiving a successful payment event:

```text
Razorpay
    ↓
Payment Webhook
    ↓
Payment Service
    ↓
Payment Successful
    ↓
Feign Client
    ↓
Ride Service
    ↓
Update Ride Payment Status
```

This ensures that the Ride Service knows when the corresponding payment has actually succeeded.

---

# 🔄 Inter-Service Communication

The project uses **Spring Cloud OpenFeign** for communication between microservices.

For example:

```text
Payment Service
       │
       │ Feign Client
       ▼
Ride Service
```

Feign allows the services to communicate through declarative HTTP clients instead of manually creating HTTP requests.

---

# 🌐 API Gateway

An API Gateway has been added to provide a single entry point to the microservices.

```text
Client
  │
  ▼
API Gateway
  │
  ├── Auth Service
  ├── User Service
  ├── Driver Service
  ├── Ride Service
  └── Payment Service
```

The Gateway routes requests to the appropriate microservice.

---

# 🧭 Service Registry

The project uses **Netflix Eureka** as the Service Registry.

Each microservice registers itself with Eureka.

```text
                  Eureka
                    │
        ┌───────────┼───────────┐
        │           │           │
        ▼           ▼           ▼
      Auth         Ride       Payment
     Service      Service     Service
```

This allows services to discover each other without relying entirely on hardcoded service URLs.

---

# ⚡ Kafka

Apache Kafka has been added to the project for event-driven communication.

Kafka infrastructure has been configured using Docker/Kafka setup.

The purpose is to provide asynchronous communication between services where required.

The project has also involved Kafka broker/controller configuration using the **KRaft architecture**.

---

# 🔒 Concurrency Handling

Ride booking introduces a concurrency problem when multiple drivers attempt to accept the same ride simultaneously.

For example:

```text
Driver A ───────┐
                │
                ├──► Ride #123
                │
Driver B ───────┘
```

Both requests cannot successfully accept the same ride.

The project addresses this using database concurrency concepts.

### Pessimistic Locking

A database row can be locked while processing the ride acceptance transaction.

Conceptually:

```text
Transaction starts
       ↓
Lock Ride
       ↓
Check Ride Status
       ↓
Accept Ride
       ↓
Commit
       ↓
Release Lock
```

This prevents another transaction from modifying the same ride simultaneously.

### Optimistic Locking

Entity versioning can also be used to detect concurrent updates.

Example:

```java
@Version
private Long version;
```

If another transaction modifies the entity first, the stale transaction can fail instead of overwriting the newer data.

---

# 🗄️ Database

The application uses relational databases for persistent application data.

The project has used:

* PostgreSQL
* MySQL
* JPA/Hibernate

Persistent business data includes entities such as:

```text
User
Driver
Ride
Payment
```

Redis is used separately for fast driver-location/geospatial operations.

---

# 🐳 Docker

Docker is used for running infrastructure components required by the application.

The project has included containerized infrastructure such as:

```text
Redis
Kafka
PostgreSQL
pgAdmin
```

Docker Compose can be used to manage multiple infrastructure containers together.

---

# 🧪 API Testing

The APIs have been tested using **Postman**.

Testing includes:

* Registration
* Login
* JWT authentication
* Driver APIs
* Driver location updates
* Nearby driver search
* Ride creation
* Ride acceptance
* Ride status updates
* OTP verification
* Payment order creation
* Razorpay webhook
* Payment status updates
* Protected APIs

---

# 🔄 Complete Ride Flow

The current implemented flow can be represented as:

```text
                    User
                     │
                     ▼
              Authentication
                     │
                     ▼
                 JWT Token
                     │
                     ▼
               Create Ride
                     │
                     ▼
              Ride Service
                     │
                     ▼
          Search Nearby Drivers
                     │
                     ▼
              Redis GEO
                     │
                     ▼
              Driver Found
                     │
                     ▼
            Driver Accepts Ride
                     │
                     ▼
             OTP Verification
                     │
                     ▼
                Ride Starts
                     │
                     ▼
               Ride Completes
                     │
                     ▼
             PAYMENT_PENDING
                     │
                     ▼
             Create Razorpay Order
                     │
                     ▼
            User Makes Payment
                     │
                     ▼
             Razorpay Webhook
                     │
                     ▼
          Verify Webhook Signature
                     │
                     ▼
           Payment Successful
                     │
                     ▼
            Payment Service
                     │
                  Feign
                     │
                     ▼
              Ride Service
                     │
                     ▼
          Update Payment Status
```

---

# 🛠️ Technology Stack

| Technology           | Usage                          |
| -------------------- | ------------------------------ |
| Java                 | Backend development            |
| Spring Boot          | Microservices                  |
| Spring Security      | Security                       |
| JWT                  | Authentication                 |
| Spring Data JPA      | Persistence                    |
| Hibernate            | ORM                            |
| PostgreSQL           | Database                       |
| MySQL                | Database                       |
| Redis                | Driver geolocation             |
| Apache Kafka         | Event-driven communication     |
| OpenFeign            | Inter-service communication    |
| Netflix Eureka       | Service discovery              |
| Spring Cloud Gateway | API Gateway                    |
| Razorpay             | Payment gateway                |
| Docker               | Containerization               |
| Maven                | Build management               |
| Postman              | API testing                    |
| Ngrok                | Local Razorpay webhook testing |

---

# 📁 Project Structure

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
├── api-gateway/
│
├── service-registry/
│
└── docker-compose.yml
```

---

# 🔑 Core Backend Concepts Implemented

This project currently demonstrates practical backend concepts including:

* RESTful APIs
* Microservices architecture
* JWT authentication
* Spring Security
* Role-based authorization
* Service discovery
* API Gateway
* OpenFeign
* Redis Geospatial operations
* Driver location tracking
* Distance calculation
* Fare calculation
* OTP verification
* Ride lifecycle management
* Payment gateway integration
* Razorpay order creation
* Razorpay webhooks
* Webhook signature verification
* Payment state management
* Inter-service communication
* Database transactions
* Pessimistic locking
* Optimistic locking
* Kafka infrastructure
* Dockerized infrastructure

---

# 🎯 Project Objective

The objective of this project is to build a **real-world ride-booking backend using a distributed microservices architecture**.

The application combines:

```text
Authentication
      +
Microservices
      +
Location Tracking
      +
Ride Management
      +
Concurrency Control
      +
Payment Processing
      +
Redis
      +
Kafka
      +
Docker
```

to create a backend system that models the core functionality of a modern ride-booking platform.
