# Logistics Platform API

Spring Boot 3 / Java 17 backend for shipments, warehouses, tracking, delivery
partners and pricing. This is your uploaded project with the bugs below fixed,
the missing pieces added so it actually compiles and runs, and several new
features layered on top.

## Running it

**Docker (recommended):**
```bash
cp .env.example .env      # fill in MYSQL_ROOT_PASSWORD and APP_JWT_SECRET
docker compose up --build
```
API comes up on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

**Locally (needs MySQL + Redis running):**
```bash
mvn spring-boot:run
```
Set `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `APP_JWT_SECRET`, etc as
environment variables, or edit the defaults in `application.yml` for local dev.

On first boot, if `APP_ADMIN_DEFAULT_PASSWORD` isn't set, a random admin
password is generated and printed once to the log — copy it from there.

---

## Bugs fixed

**Would not compile at all**
- `ResourceNotFoundException` was referenced everywhere (`ShipmentService`,
  `WarehouseService`, `TrackingService`, `GlobalExceptionHandler`) but the class
  itself was never included — added it.
- Every JPA repository (`UserRepository`, `RoleRepository`, `PermissionRepository`,
  `ShipmentRepository`, `WarehouseRepository`, `ShipmentTrackingEventRepository`,
  `PriceRuleRepository`, `AuditLogRepository`, `DeliveryPartnerRepository`) was
  missing — added all of them.
- No `pom.xml` was included — added one with all dependencies the code actually
  imports (Spring Security, Data JPA, Redis, Validation, JJWT, MySQL driver, etc).

**Silent/incorrect behavior**
- `GlobalExceptionHandler` imported `org.apache.coyote.BadRequestException`
  (Tomcat's internal class) instead of this project's own
  `exception.BadRequestException` — the app's own `BadRequestException` was
  never actually caught and fell through to a generic 500.
- No handler existed for `BusinessException`, `IllegalArgumentException`,
  `IllegalStateException`, or `UnauthorizedException` — duplicate-email
  registration, insufficient warehouse capacity, invalid pricing input, and bad
  login attempts all surfaced as an opaque 500 instead of the correct 4xx.
- `AuthService.login()` normalized the email everywhere *except* right before
  calling `AuthenticationManager.authenticate()` — logging in with different
  casing than was used at registration failed even with the correct password.
- `DataInitializer` only ever assigned permissions to `CUSTOMER` and `ADMIN`;
  `OPERATOR` and `DELIVERY_PARTNER` roles were left with zero permissions, so an
  operator account was blocked by `@PreAuthorize hasAuthority(...)` checks on
  endpoints their role should clearly be allowed to use.
- `ShipmentService.cancel()` and `TrackingService.add()` had **no ownership
  check at all** — any authenticated customer could cancel or view *any other
  customer's* shipment (including their addresses) just by knowing/guessing a
  tracking number or shipment id. Both are now restricted to the shipment's
  owner or staff (ADMIN/OPERATOR).
- Cancelling a shipment never released the warehouse capacity (or delivery
  partner capacity) it had reserved — that space was lost permanently. Fixed.
- `TrackingService.add()` accepted *any* status for *any* shipment in *any*
  state (e.g. jumping straight from `CREATED` to `DELIVERED`, or "updating" an
  already-cancelled shipment) — added an explicit status state machine.
- `CustomUserDetailsService` had a missing space in its error message
  (`"...with email"+email` → `"...with email " + email`).
- `docker-compose.yml` set `JWT_SECRET`/`JWT_EXPIRATION_MS` env vars, which do
  **not** match Spring Boot's relaxed-binding name for `app.jwt.secret`
  (`APP_JWT_SECRET`) — the container silently used the hardcoded default
  secret no matter what was configured.
- `Dockerfile` did `COPY target/logistics-platform-1.0.0.jar` assuming a jar
  had already been built on the host — `docker build .` alone did nothing.
  It's now a real multi-stage build.

**Security/hygiene**
- The MySQL root password and JWT signing secret were hardcoded in
  `application.yml` and `docker-compose.yml` and committed to the repo. Moved
  to environment variables (`.env`, gitignored) with a `.env.example` template.
- The seeded admin account had a hardcoded password
  (`Admin@12345`) — now generated randomly per-environment unless you
  explicitly set one, and logged once on first boot only.
- `AuditService.record()` could throw and roll back the *business* transaction
  it was meant to be logging (e.g. a DB hiccup while writing an audit row would
  cancel a shipment cancellation). Audit writes are now best-effort.
- `warehouse.Controller` / `warehouse.DTO` used inconsistent capitalized
  package names versus every other feature (`controller`/`dto`) — normalized.

## Features added
- **Refresh tokens & logout**: `/api/v1/auth/refresh` and `/api/v1/auth/logout`,
  backed by Redis-stored refresh tokens and a JWT-id blacklist for revocation.
- **Delivery partner management & auto-assignment**: `DeliveryPartner` existed
  as a dead entity with no service/repository/API. Added full CRUD
  (`/api/v1/delivery-partners`) plus automatic least-loaded-partner assignment
  when a shipment goes `OUT_FOR_DELIVERY`, with capacity released on delivery
  or cancellation.
- **Shipment status state machine** enforcing valid transitions only.
- **Audit log API** (`/api/v1/audit-logs`, admin-only) — the audit trail was
  being written but was never readable anywhere.
- **Warehouse listing endpoint** (`GET /api/v1/warehouses`, paginated) — there
  was previously no way to list warehouses at all, only fetch by id.
- **CORS configuration** (`APP_CORS_ALLOWED_ORIGINS`), actually wired into the
  security filter chain.
- **OpenAPI/Swagger UI** at `/swagger-ui.html` documenting every endpoint with
  bearer-auth support.
- **Pagination guardrails** on all paginated endpoints (page/size clamped to a
  sane range) to prevent abuse via huge or negative page sizes.
- Global exception handling now also covers unique-constraint violations
  (`DataIntegrityViolationException`) and access-denied errors with clean JSON
  responses instead of leaking stack traces.

## Known limitations / follow-ups worth doing next
- Pricing still calculates distance as `0` (no geocoding/distance service is
  wired up), so `pricePerKm` has no real effect yet — flagged clearly in
  `ShipmentService.create()`.
- `ddl-auto: update` is convenient for local dev but you'll want Flyway or
  Liquibase migrations plus `ddl-auto: validate` before this goes anywhere
  production-like.
- No automated tests were included in the original upload; none were added
  here either, to keep the scope to fixes/features you asked for — happy to
  add a test suite (JUnit + Testcontainers for MySQL/Redis) as a follow-up.
