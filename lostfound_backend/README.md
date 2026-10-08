# Lost & Found — Backend (Complete)

A Spring Boot REST API for reporting lost and found items, claiming found items, and getting
**targeted** notifications when a match appears.

## Status: all phases complete

| Phase | Feature | Status |
|---|---|---|
| 0 | Project setup, health check | Done |
| 1 | Auth — register / login / refresh (JWT) | Done |
| 2 | Categories + Items CRUD | Done |
| 3 | Image upload for items | Done |
| 4 | Claims — claim a found item, admin approve/reject | Done |
| 5 | Notifications — matching engine + inbox + alert subscriptions | Done |
| 6 | Swagger docs, rate limiting, logging, Docker, tests | Done |

---

## Quick start

### Option A — Docker (easiest, no local Postgres or Java needed)
```bash
docker compose up --build
```
That starts Postgres + the app together. Open http://localhost:8080/swagger-ui.html

### Option B — Run locally against Neon (or any Postgres)

Spring Boot does **not** read `.env` automatically. Export the variables first:

**PowerShell (Windows):**
```powershell
$env:DB_URL="jdbc:postgresql://<your-neon-host>/<dbname>?sslmode=require"
$env:DB_USERNAME="<username>"
$env:DB_PASSWORD="<password>"
$env:JWT_SECRET="<a-random-string-at-least-32-characters>"
mvn spring-boot:run
```

**bash / zsh (Mac/Linux):**
```bash
export $(cat .env | xargs)
mvn spring-boot:run
```

Flyway creates every table and seeds the categories on first startup — you never run SQL by hand.

Verify: `curl http://localhost:8080/health` → `{"status":"OK"}`

> **JWT_SECRET must be at least 32 characters.** Shorter secrets throw a `WeakKeyException`
> on startup because HMAC-SHA256 requires a 256-bit key.

### Running the tests
```bash
mvn test
```
These are pure unit tests (Mockito, no Spring context), so **they run without a database** —
useful for CI and for checking the project compiles.

---

## API reference

Interactive docs: **http://localhost:8080/swagger-ui.html** (click *Authorize*, paste an access token)

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Create account, returns tokens |
| POST | `/api/v1/auth/login` | Public | Log in, returns tokens |
| POST | `/api/v1/auth/refresh` | Refresh token | New access token |

### Categories
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/categories` | Public | List categories |

### Items
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/items` | Required | Report a LOST or FOUND item |
| GET | `/api/v1/items` | Public | Paginated, filter by `itemType`, `status`, `categoryId` |
| GET | `/api/v1/items/{id}` | Public | Item detail |
| PATCH | `/api/v1/items/{id}` | Owner/admin | Partial update |
| DELETE | `/api/v1/items/{id}` | Owner/admin | Soft delete |
| POST | `/api/v1/items/{id}/images` | Owner/admin | Upload photo (multipart `file`) |

### Claims
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/items/{itemId}/claims` | Required | Claim a FOUND item |
| GET | `/api/v1/claims` | Admin | Moderation queue (`?status=PENDING`) |
| GET | `/api/v1/claims/mine` | Required | Your own claim history |
| GET | `/api/v1/claims/{id}` | Claimant/admin | Claim detail |
| PATCH | `/api/v1/claims/{id}` | Admin | `{"status":"APPROVED"}` or `REJECTED` |

### Notifications
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/notifications` | Required | Your inbox (`?unreadOnly=true`) |
| GET | `/api/v1/notifications/unread-count` | Required | Badge count |
| PATCH | `/api/v1/notifications/{id}/read` | Required | Mark one read |
| PATCH | `/api/v1/notifications/read-all` | Required | Mark all read |

### Alert subscriptions
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/subscriptions` | Required | Watch a category and/or keyword |
| GET | `/api/v1/subscriptions` | Required | Your alerts |
| DELETE | `/api/v1/subscriptions/{id}` | Owner | Remove an alert |

---

## How the notification matching works

The original spec said "notify all users" when an item is found. That doesn't scale and reads as
spam once there are more than a handful of users, so this implementation targets instead:

When a **FOUND** item is reported, `NotificationService.notifyMatchesForFoundItem` runs:

1. Extracts keywords from the item's title + description (words of 4+ characters, max 6).
2. Finds still-**OPEN LOST** reports in the **same category** whose title/description contains
   any of those keywords → notifies those reporters.
3. Finds users whose **subscriptions** match the category and/or keyword → notifies them.
4. Deduplicates, so nobody gets two notifications for the same found item, and the finder is
   never notified about their own report.

Notifications are persisted (an inbox users poll via the API). Real-time push (WebSocket / FCM /
email) is a delivery layer that can be added on top without changing this logic.

**Natural upgrade path:** replace the keyword `LIKE` matching with text embeddings stored in
**pgvector** (Neon supports it). Then "black leather billfold" matches "dark wallet" — the
matching engine is isolated in one service, so this is a contained change.

## Claim state machine

- Only `FOUND` items that aren't `RESOLVED`/`CLOSED` can be claimed.
- You can't claim your own item, or file a second pending claim on the same item.
- First claim moves the item `OPEN` → `CLAIMED`.
- **Approve** → claim `APPROVED`, item `RESOLVED`, competing pending claims auto-`REJECTED`
  (and each of those claimants is notified).
- **Reject** → claim `REJECTED`; if nothing else is pending, item reverts to `OPEN`.

## Making yourself an admin

There's no admin self-signup (deliberately). Promote a user directly in the DB:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

---

## Deploying to Render

1. Push this repo to GitHub.
2. On Render: **New → Web Service → connect your repo**. It detects the `Dockerfile`.
3. Set environment variables in Render's dashboard: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
   `JWT_SECRET`. Do **not** set `PORT` — Render injects it and `application.yml` already reads it.
4. Note the free tier sleeps after ~15 min idle, so the first request after a nap takes
   30–50s (JVM cold start).

**Important for deployment:** uploaded images are written to local disk, which is **ephemeral** on
Render — files vanish on redeploy. Before real users touch this, swap `LocalFileStorageService`
for an S3/Cloudinary implementation of `FileStorageService` (that interface exists precisely so
this is a one-class change).

---

## Architecture notes & deliberate trade-offs

- **Soft deletes** — items carry `is_deleted` rather than being removed, keeping history for audit.
- **UUIDs** for `users`, `items`, `claims`, `notifications` (externally referenced); auto-increment
  ints for `categories` / `item_images` / `user_subscriptions` (internal lookup rows).
- **Flyway** is the single source of truth for schema; JPA runs with `ddl-auto: validate` so a
  drifting entity fails fast at startup instead of silently altering tables.
- **Refresh tokens are long-lived JWTs, not revocable.** Storing a hash in Postgres/Redis to
  support revocation is the main security upgrade left.
- **Rate limiting is in-memory and per-instance** (`RateLimitFilter`, 15 writes/min by default).
  Move it to Redis if you ever run more than one instance. It also runs before Spring Security
  populates the context, so anonymous routes are limited by IP rather than user id.
- **No email verification yet** — users are created with `is_verified = false` and the column is
  there, but no mail flow is wired up.
- **Tests cover service logic, not controllers/repositories.** The state machines (claims,
  matching, authorization) are the parts most worth testing; adding `@WebMvcTest` controller tests
  and Testcontainers integration tests is the next testing step.

## Known limitation

This project was written in an environment without access to Maven Central, so it was **never
compiled or test-run**. Every file was reviewed by hand, but run `mvn clean test` first and report
any compile error — those are quick fixes.
