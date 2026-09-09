# Retailer Rewards Program API

A Spring Boot REST service that calculates customer reward points earned on
purchase transactions, broken down by month and totalled over a trailing
period.

## Problem

A customer earns:
- **2 points** for every dollar spent **over $100** in a transaction
- **1 point** for every dollar spent **between $50 and $100** in a transaction
- **0 points** for the first $50

Example: a $120 purchase = (2 × $20) + (1 × $50) = **90 points**.

Given a customer's transactions, the API calculates points earned per month
and the total over the period.

## Design details

- **Layering**: `controller` → `service` → `repository`, standard Spring
  separation of concerns.
  - `RewardsCalculationService` — the points formula only, pure and
    stateless, so it's trivial to unit test in isolation from everything
    else.
  - `TransactionDataService` — simulates an **asynchronous** fetch of a
    customer's transactions (`@Async` + `CompletableFuture`), standing in
    for a call to a downstream transactions microservice or a slow DB
    query, per the "simulate an asynchronous API call" requirement.
  - `RewardsService` — orchestrates: validates the customer exists, awaits
    the async fetch, filters to the requested window, groups by calendar
    month, and applies the formula.
  - `TransactionStore` — in-memory data store, seeded once at startup from
    `src/main/resources/data/seed-data.json`. Swapping this for a real
    database/JPA repository later wouldn't require any change to the
    service layer above it.
- **Dynamic time window**: the assignment's "three month period" is exposed
  as a `months` query parameter (default `3`) rather than hard-coded, so
  the same endpoint scales to any window. An optional `asOfDate` parameter
  lets you pin the "today" the window is measured back from, which is what
  makes the sample requests below reproducible against the bundled dataset.
- **Money math**: amounts and point math use `BigDecimal`, not `double`, to
  avoid floating-point rounding errors on currency.
- **Errors**: a `@RestControllerAdvice` (`GlobalExceptionHandler`) maps
  domain exceptions to proper HTTP status codes (404 for unknown customer,
  400 for invalid parameters, 503 if the async fetch fails) with a
  consistent JSON error body, instead of leaking stack traces.

## Technical details

- Java 8, Spring Boot 2.7.x (`spring-boot-starter-web`,
  `spring-boot-starter-validation`), Maven
- No database — an in-memory store seeded from a bundled JSON fixture, per
  the assignment's "make up a data set" instruction
- JUnit 5 + Mockito for tests

## API details

Base path: `/api/v1/rewards`

### `GET /customers/{customerId}`

| Param | Required | Default | Description |
|---|---|---|---|
| `months` | no | `3` | Size of the trailing window, in months |
| `asOfDate` | no | today | Reference date the window is measured back from (`yyyy-MM-dd`) |

**Example** (against the bundled dataset, which spans June–August 2026):

```
GET /api/v1/rewards/customers/C001?months=3&asOfDate=2026-09-08
```

```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "periodStart": "2026-06-09",
  "periodEnd": "2026-09-08",
  "totalPointsEarned": 324,
  "monthlyBreakdown": [
    {
      "month": "2026-06",
      "pointsEarned": 90,
      "transactions": [
        { "transactionId": "T0002", "transactionDate": "2026-06-05", "amount": 120.00, "pointsEarned": 90 }
      ]
    }
  ]
}
```

### `GET /customers`

Same `months` / `asOfDate` parameters; returns the array of the response
above for every customer in the dataset.

### Errors

```json
{
  "timestamp": "2026-09-08T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "No customer found with id 'C999'",
  "path": "/api/v1/rewards/customers/C999"
}
```

## Sample customers (seeded dataset)

`C001` Alice Johnson · `C002` Brian Smith · `C003` Carla Diaz — transactions
span June–August 2026, plus one May transaction on C001 to demonstrate that
purchases outside the requested window are correctly excluded.

## Running it

```bash
mvn spring-boot:run
```

Then, e.g.:

```bash
curl "http://localhost:8080/api/v1/rewards/customers/C001?asOfDate=2026-09-08"
curl "http://localhost:8080/api/v1/rewards/customers?months=3&asOfDate=2026-09-08"
```

## Testing

```bash
mvn test
```

- `RewardsCalculationServiceTest` — the points formula, boundary values
  ($0, $50, $50.01, $100, $100.01, fractional dollars) and the worked
  example from the spec ($120 → 90 points)
- `RewardsServiceTest` — month grouping, window filtering (excludes
  out-of-window transactions), unknown-customer handling
- `RewardsControllerTest` — HTTP status codes and JSON shape via `MockMvc`

## Assumptions

- "Between $50 and $100" is treated as inclusive of $100 in the higher
  tier's boundary and exclusive at $50 (i.e. exactly $50 earns 0, exactly
  $100 earns 50) — the natural reading of the worked example in the spec.
- Points are whole numbers; fractional cents within a tier round down
  rather than up, so a customer never earns more than the formula strictly
  allows.
- A customer with no transactions in the requested window returns
  `totalPointsEarned: 0` and an empty `monthlyBreakdown`, rather than a 404.
