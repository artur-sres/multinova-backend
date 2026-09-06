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
- JWT
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

### Cash entries and operation history

Migration V22 adds cash entries. `POST /cash-entries` accepts `title`, `kind`
(`LOAN`, `CAPITAL`, `OTHER`), a positive `amount`, `receivedOn` (today or earlier),
and optional `description`. `GET /cash-entries` returns the user's latest 100
entries by receipt date. These endpoints always use the authenticated user.

The dashboard adds `otherIncome`, summed by receipt date, and calculates
`periodBalance = received + otherIncome - purchased - billsPaid`. Cash entries do
not change sales, customer debt, or bills. Loan repayments are separate bills.

Migration V23 creates an indexed operation history. PostgreSQL triggers capture
snapshots inside the source transaction for sales, cancellations, stock purchases,
customer/product creation and personal/catalog changes, customer payments,
bill creation/payment/deletion, and cash entries. Stock/debt changes alone do not
generate duplicate edit events. A rolled-back transaction leaves no history.
The history has no foreign key to the source entity, so later deletion preserves
the snapshot. `GET /history` returns only the authenticated user's latest 100
events, ordered by operation time and ID descending.

Existing recoverable events are backfilled and marked `recovered`. Their dates
come from surviving records (including the last update date for old cancelled
sales). Previously deleted records and unrecorded operations cannot be recovered.
Backfill does not create financial movements or modify existing balances.

Verification: `mvn -Dtest=*ServiceTests test` covers cash validation, user scope,
and dashboard arithmetic. `src/test/test-history.ps1` runs all migrations and the
SQL regression checks against an isolated PostgreSQL 18 cluster initialized at
`target/history-test-pg`, listening only on localhost port 55439. It checks the
cluster's data directory before writing and stops the test server on completion.
The SQL checks cover recovery, rollback, cancellation/payment idempotence,
deletion preservation, snapshots, scope, dates and the 100-event limit.

### Bills

Bills accept an optional `description` in `POST /bills`, returned by the listing
and detail responses. Migration `V21__add_bill_description.sql` adds the nullable
text column, preserving existing bills. Responses also include `paidAt`.

Bill details are immutable. `PATCH /bills/{id}/pay` records payment once, and
`DELETE /bills/{id}` removes the bill. Pending bills contribute to the dashboard's
global payable total; paid bills contribute to period expenses according to
`paidAt`, not their due date. Deleting a paid bill also removes it from the paid
total for that period. All operations are scoped to the authenticated user.

### Inventory entries and the dashboard

`POST /products` with positive `stock` now records an initial stock purchase using
`stock × price.purchasePrice`, dated at creation time. Product creation, pricing,
and the purchase share one transaction; a failure rolls back the whole operation.
Creating a product with zero stock does not create a purchase.

Subsequent entries use `POST /stock-purchases`, supplying the quantity and actual
unit cost of that purchase. The dashboard includes both initial and subsequent
purchases in `purchased` and subtracts them in `periodBalance`. Historical purchase
amounts are independent of changes to the product's price records.

`PATCH /products/{id}` rejects changes to the stock balance with HTTP 409. An
unchanged stock value remains accepted for compatibility. Stock movements must
go through purchases or sales so that balances cannot be changed silently.

Existing products are not backfilled automatically: quantities, costs and dates
from before this fix must be reconciled against real purchase records first.

Service regression tests: `mvn -Dtest=*ServiceTests test`.

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
