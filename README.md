# MultiNova API

**Backend REST API for MultiNova, a small-business management system for products, inventory, customers, sales, and bills.**

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-336791)
![License](https://img.shields.io/badge/status-personal%20project-lightgrey)

The application is built with Spring Boot and PostgreSQL. It uses JWT authentication, email verification and password recovery, Flyway database migrations, Bean Validation, and MapStruct.

---

## Table of Contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Requirements](#requirements)
- [API overview](#api-overview)
- [Project structure](#project-structure)


---

## Features

- User registration, email verification, login, and password recovery
- JWT-protected API endpoints
- Product, category, and brand management
- Historical purchase and sale prices per product
- Customer management
- Sales with line items and automatic stock validation
- Bill creation, listing, and payment tracking
- User-scoped data access
- Centralized validation and error responses

## Tech stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- JJWT
- MapStruct
- Maven Wrapper

## Requirements

- Java Development Kit (JDK) 21
- PostgreSQL running locally or accessible over the network
- A Mailtrap SMTP inbox for development email delivery

You do not need to install Maven separately; the repository includes the Maven Wrapper.

## API overview

| Resource | Method and path | Description | Public |
| --- | --- | --- | :---: |
| Authentication | `POST /auth/login` | Log in | Yes |
| Authentication | `POST /auth/verify` | Verify an email address | Yes |
| Authentication | `POST /auth/forgot-password` | Request a password reset code | Yes |
| Authentication | `POST /auth/reset-password` | Reset a password | Yes |
| Users | `POST /users` | Register a user | Yes |
| Users | `GET /users/{id}` | Get a user | No |
| Users | `PATCH /users/{id}` | Update a user | No |
| Products | `POST /products` | Create a product | No |
| Products | `GET /products` | List products | No |
| Products | `GET /products/{id}` | Get a product | No |
| Products | `PATCH /products/{id}` | Update a product | No |
| Products | `DELETE /products/{id}` | Soft-delete a product | No |
| Prices | `POST /products/{productId}/prices` | Add a price | No |
| Prices | `GET /products/{productId}/prices` | List price history | No |
| Prices | `GET /products/{productId}/prices/current` | Get the current price | No |
| Categories | `POST /categories` | Create a category | No |
| Categories | `GET /categories` | List categories | No |
| Categories | `GET /categories/{id}` | Get a category | No |
| Categories | `PUT /categories/{id}` | Replace a category | No |
| Categories | `DELETE /categories/{id}` | Delete a category | No |
| Brands | `POST /brands` | Create a brand | No |
| Brands | `GET /brands` | List brands | No |
| Brands | `GET /brands/{id}` | Get a brand | No |
| Brands | `PUT /brands/{id}` | Replace a brand | No |
| Brands | `DELETE /brands/{id}` | Delete a brand | No |
| Customers | `POST /customers` | Create a customer | No |
| Customers | `GET /customers` | List customers | No |
| Customers | `GET /customers/{id}` | Get a customer | No |
| Customers | `PATCH /customers/{id}` | Update a customer | No |
| Customers | `DELETE /customers/{id}` | Delete a customer | No |
| Sales | `POST /sales` | Create a sale | No |
| Sales | `GET /sales` | List sales | No |
| Sales | `DELETE /sales/{id}` | Cancel a sale | No |
| Bills | `POST /bills` | Create a bill | No |
| Bills | `GET /bills` | List bills | No |
| Bills | `GET /bills/{id}` | Get a bill | No |
| Bills | `PATCH /bills/{id}/pay` | Mark a bill as paid | No |

## Project structure

```text
src/main/java/com/artur_sres/multinova/
|-- config/       Security and application configuration
|-- controller/   HTTP endpoints
|-- domain/       JPA entities and enums
|-- dto/          Request and response models
|-- exception/    Domain exceptions and global error handling
|-- mapper/       MapStruct mappers
|-- repository/   Spring Data repositories
|-- security/     JWT authentication filter and user context
|-- service/      Business rules and use cases
`-- util/         Shared utilities

src/main/resources/
|-- application.properties
`-- db/migration/ Flyway SQL migrations
```
