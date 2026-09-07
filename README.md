# MultiNova API

### Customer summaries, exports and discounts

Migration V29 preserves product names on sale items and customer names on ledger
entries. Existing names are recovered from the available current records and
explicitly marked as recovered; subsequent renames do not rewrite new snapshots.
The customer statement includes sale items (quantity, unit price and subtotal),
source references, cancellation status and linked reversal reasons. Balances
continue to follow recording order, independently of the effective date.

Authenticated receipt endpoints:

- `GET /customers/{customerId}/statement/{entryId}/receipt`
- `GET /sales/{id}/receipt`
- `GET /customers/{customerId}/statement/export?limit=50` (1–500 most recent entries)

Receipts are available for customer purchases (sales) and payments, including
explicit cancelled/reversed status. They include the current customer name and
balance in a consistent snapshot at issuance, alongside the historical entry.
The customer-facing summary shows the action, products, discounts and current
amount due, available credit or settled balance. A purchase never declares payment; payments apply
to the customer's overall balance and are not allocated to individual sales.
These are symbolic, non-fiscal records. No public links or messaging service is
created. The frontend lets the user share/copy text, download text or PNG, or print/PDF.
History export includes purchases, payments, cancellations and other account
movements with item details, and explicitly signals when older entries are omitted.

Migration V31 adds per-item discounts. `POST /sales` accepts `discountType`
(`NONE`, `PERCENTAGE`, `AMOUNT`) and `discountValue` on each item. The original
`unitPrice` is preserved. Discounts apply to `quantity × unitPrice`; percentages
round once per line to cents (HALF_UP), and fixed amounts are per line, not per
unit. The server validates limits and calculates `discountAmount`, `subtotal`
and the sale total. Debt, dashboard totals and cancellation use the net total.
Old items retain zero discount. Deploy frontend and backend together.

History filters apply automatically with a 300 ms debounce. Period filters on
the dashboard, bills and sales apply on valid changes. History, customer data,
statements and dashboard queries refresh after successful mutations, changes
in another tab, reconnecting or returning to the app. Requests are cancelled
when superseded; transient failures retry twice without continuous polling.

Validation: `powershell -ExecutionPolicy Bypass -File src/test/test-history.ps1 -StatementTests`
applies all migrations to an isolated test database and tests item/name snapshots,
balances, reversals, cancellation states and receipt ownership. Run the frontend
checks with `npm run lint`, `npm run build`, `npm test`; after building, Windows
users with Edge can run `node tests/browser-history.cjs` for a mocked-API browser
test of automatic search, pagination reset, statement details, sharing and PNG.

### Local product and customer images

Product and customer forms accept JPEG/PNG uploads up to 5 MB. The backend
checks the decoded format and rejects images over 20 megapixels, then resizes
to at most 1200 pixels per side and writes a fresh PNG without original metadata.
No cloud account or paid service is required.

Start the backend from `multinova-backend`; images are created in `uploads/`
inside that working directory. The directory is ignored by Git and survives
rebuilds. Set `IMAGE_STORAGE_DIRECTORY` to an absolute path if launching the
backend from different working directories. Keep the database and this folder
together when moving or backing up local data.

Flyway migration V28 adds nullable image keys, preserving existing image URLs.
Restart the backend to apply it. `PUT` (multipart field `file`), `GET`, and
`DELETE` are available at `/products/{id}/image` and `/customers/{id}/photo`.
Every operation requires JWT authentication and checks record ownership. The
frontend fetches private images with the Authorization header and displays a
temporary browser object URL; the uploads directory is not publicly served.

Replacing/removing a photo deletes the previous local file after database
commit; a rolled-back replacement deletes the new file. Abrupt process or disk
failures can still leave orphan files. Soft-deleted products retain their files
but cannot serve them. This implementation is intended for local development.

When creating a record, its data is saved before the photo. If uploading fails,
the form reports that the record was saved and retries using its existing ID.

Validation: `mvn "-Dtest=ImageStorageServiceTests,CatalogImageServiceTests" test`
covers file validation, resizing, persistence, ownership checks and transaction
cleanup. Frontend checks: `npm run lint`, `npm run build`, and `npm test`.

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
`stock × price.purchasePrice`, dated at creation time, as an `ADJUSTMENT` without payment. Product creation, pricing,
and the purchase share one transaction; a failure rolls back the whole operation.
Creating a product with zero stock does not create a purchase.

Subsequent entries use `POST /stock-purchases`, supplying the quantity and actual
unit cost of that purchase, with `entryType`: `ADJUSTMENT` (default, no cash effect),
`PAID` (paid today), or `PENDING` (requires `dueDate` and creates a pending bill in
the same transaction). The dashboard includes only `PAID` entries in `purchased`.
Paying a pending bill affects `billsPaid` on its payment date, without counting
the stock entry twice. Migration V30 preserves existing entries as `PAID` because
their original payment state is unknown. Noncash entries use `STOCK_ENTRY` in
history and are excluded from financial filters. Historical purchase
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
