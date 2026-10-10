# Lost & Found

A full-stack web application for reporting lost and found items. People post what they've lost or found, get notified when a found item plausibly matches something they lost, and can claim found items. Claims go through a moderation flow.

Live demo:https://lost-and-found-alpha-ruby.vercel.app
API docs (Swagger): https://lost-and-found-3xd2.onrender.com/swagger-ui.html

> The backend runs on Render's free tier, so the first request after a period of inactivity can take 30-60 seconds while it wakes up.


## Features

Authentication: register and login with JWT access tokens (15 min) and refresh tokens (7 days). The frontend refreshes the access token automatically when it expires.
Lost and found reports:create, edit, and soft-delete reports. Filter the feed by type, status, and category, with pagination.
Photo upload:drag-and-drop with per-file progress, multiple photos per item.
Claims:claim a found item by describing proof of ownership. Admins approve or reject claims from a moderation queue.
Targeted notifications:when a found item is reported, only users with a matching open lost report or a matching alert subscription are notified (not everyone).
Alert subscriptions:watch a category, a keyword, or both.
Role-based access:`USER` and `ADMIN` roles, enforced on the server.
Responsive UI:sidebar on desktop, drawer on mobile, loading, empty, and error states, toast notifications.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React, Vite, React Router, Axios, plain CSS |
| Backend | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Database | PostgreSQL, schema managed by Flyway migrations |
| Auth | JWT (jjwt), BCrypt password hashing |
| Image storage | Cloudinary |
| API docs | springdoc-openapi (Swagger UI) |
| Hosting | Vercel (frontend), Render (backend), Neon (database) |

## Architecture

```
Browser (React on Vercel)
        |  HTTPS + JWT in Authorization header
        v
Spring Boot API (Render)
   Filters:  RateLimit -> CORS -> JWT auth
   Controller -> Service -> Repository
        |                         |
        v                         v
PostgreSQL (Neon)           Cloudinary (item photos)
```

Controllers only translate HTTP into method calls. All business rules (who can claim what, when to notify whom) live in the service layer, which is what the unit tests exercise.

## Repository structure

```
.
├── backend/            Spring Boot API
│   ├── src/main/java/com/lostfound/
│   │   ├── config/         security, CORS, OpenAPI, static resources
│   │   ├── controller/     REST endpoints
│   │   ├── dto/            request and response objects
│   │   ├── entity/         JPA entities and enums
│   │   ├── exception/      custom exceptions and global handler
│   │   ├── repository/     Spring Data JPA interfaces
│   │   ├── security/       JWT util, auth filter, rate limiter
│   │   └── service/        business logic, file storage
│   ├── src/main/resources/db/migration/   Flyway SQL migrations
│   └── src/test/           unit tests
└── frontend/           React app
    └── src/
        ├── components/     reusable UI pieces
        ├── context/        auth, toast, breadcrumb state
        ├── hooks/          useUnreadCount, useDocumentTitle
        ├── layouts/        app shell
        ├── pages/          one file per screen
        ├── services/       one API module per backend resource
        ├── styles/         design system CSS
        └── utils/          validation, error messages, JWT decoding
```

## API overview

Full interactive docs are in Swagger UI. Summary:

| Area | Endpoints |
|---|---|
| Auth | `POST /api/v1/auth/register`, `/login`, `/refresh` |
| Categories | `GET /api/v1/categories` |
| Items | `GET/POST /api/v1/items`, `GET/PATCH/DELETE /api/v1/items/{id}`, `POST /api/v1/items/{id}/images` |
| Claims | `POST /api/v1/items/{itemId}/claims`, `GET /api/v1/claims/mine`, `GET /api/v1/claims/{id}`, `GET /api/v1/claims` (admin), `PATCH /api/v1/claims/{id}` (admin) |
| Notifications | `GET /api/v1/notifications`, `GET /unread-count`, `PATCH /{id}/read`, `PATCH /read-all` |
| Subscriptions | `GET/POST /api/v1/subscriptions`, `DELETE /api/v1/subscriptions/{id}` |
| Health | `GET /health` |

Browsing items and categories is public. Everything else requires a valid access token.

## How claims work

- Only `FOUND` items that are `OPEN` or `CLAIMED` can be claimed.
- You cannot claim an item you reported, or submit a second pending claim on the same item. Multiple users can claim the same item.
- The first claim moves the item from `OPEN` to `CLAIMED`.
- An admin approves a claim: the claim becomes `APPROVED`, the item becomes `RESOLVED`, and all other pending claims on that item are automatically rejected.
- An admin rejects a claim: if no other claims are pending, the item goes back to `OPEN`.
- Every decision notifies the affected claimant.

## How notification matching works

When a `FOUND` item is created, the backend:

1. Extracts keywords from its title and description (words of 4+ characters, up to 6).
2. Finds open `LOST` reports in the same category that mention any of those keywords, and notifies their owners.
3. Finds alert subscriptions matching the item's category and/or keyword, and notifies those users.
4. De-duplicates, and never notifies the person who reported the item.

## Running locally

Prerequisites: Java 17+, Maven 3.8+, Node 18+, a PostgreSQL database.

### Backend

Spring Boot does not read `.env` files automatically. Set the variables in your shell first.

PowerShell:
```powershell
cd backend
$env:DB_URL="jdbc:postgresql://localhost:5432/lostfound"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="a-random-string-of-at-least-32-characters"
$env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
$env:CLOUDINARY_API_KEY="your_api_key"
$env:CLOUDINARY_API_SECRET="your_api_secret"
mvn spring-boot:run
```

bash / zsh:
```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/lostfound
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=a-random-string-of-at-least-32-characters
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
mvn spring-boot:run
```

Flyway creates all tables and seeds the categories on first start.
Check the health endpoint: `http://localhost:8081/health`
Swagger UI: `http://localhost:8081/swagger-ui.html`

Uploaded images are stored in Cloudinary. Configure the Cloudinary environment variables before starting the backend.

### Frontend

```bash
cd frontend
npm install
cp .env.example .env     # set VITE_API_BASE_URL to your backend URL
npm run dev
```

Open `http://localhost:5173`.

### Tests

```bash
cd backend
mvn test
```

The unit tests mock the database, so they run without a Postgres instance.

## Environment variables

### Backend

| Variable | Required | Description |
|---|---|---|
| `DB_URL` | yes | JDBC URL, e.g. `jdbc:postgresql://host/db?sslmode=require` |
| `DB_USERNAME` | yes | Database user |
| `DB_PASSWORD` | yes | Database password |
| `JWT_SECRET` | yes | At least 32 characters. Never reuse the default |
| `PORT` | no | Server port (Render sets this automatically) |
| `CLOUDINARY_CLOUD_NAME` | yes | Your Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | yes | Your Cloudinary API key |
| `CLOUDINARY_API_SECRET` | yes | Your Cloudinary API secret. Keep it private |
| `JWT_ACCESS_EXP` | no | Access token lifetime in ms (default 900000) |
| `JWT_REFRESH_EXP` | no | Refresh token lifetime in ms (default 604800000) |
| `RATE_LIMIT_ENABLED` | no | Default `true` |
| `RATE_LIMIT_MAX` | no | Write requests per minute (default 15) |
| `CORS_ORIGINS` | no | Allowed frontend origin(s). Defaults to `http://localhost:5173` |

### Frontend

| Variable | Description |
|---|---|
| `VITE_API_BASE_URL` | Base URL of the backend API |

## Deployment

Database (Neon): create a project and copy the connection details into `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Migrations run automatically on backend startup.

Backend (Render): create a Web Service from the `backend` directory (the included `Dockerfile` is used) and set the environment variables above. Render injects `PORT`, which the app reads.

Images (Cloudinary): Render's free-tier disk is ephemeral, so local file storage loses uploads on every restart. Production uploads photos to Cloudinary, which stores them permanently and serves them over HTTPS. Create a free Cloudinary account and set `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET` on the backend.

Frontend (Vercel): import the `frontend` directory and set `VITE_API_BASE_URL`. A `vercel.json` rewrite sends every path to `index.html`, so refreshing on a client-side route like `/items/123` doesn't 404:

```json
{
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

### Creating an admin

There is no admin signup. Register normally, then promote the user in the database and log in again (the role is carried in the JWT):

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

## Design decisions

- **Targeted notifications instead of broadcast:** Notifying everyone about every found item doesn't scale and can feel like spam.
- **DTOs instead of returning entities:** Avoids serialization problems and accidentally exposing sensitive fields such as password hashes.
- **Soft delete:** Items are marked `is_deleted` rather than permanently removed, preserving history.
- **Flyway with `ddl-auto: validate`:** Database schema changes are versioned, and mismatches fail at startup rather than silently altering tables.
- **`FileStorageService` interface:** Image storage is abstracted behind an interface. The current implementation uploads images to Cloudinary and returns secure image URLs.

## Known limitations

- Notifications are polled every 30 seconds, not pushed over WebSockets.
- Matching is keyword-based, so "black wallet" won't match "dark leather billfold". Semantic search with embeddings (pgvector) is the natural upgrade.
- The search box filters only the currently loaded page. The backend has no full-text search endpoint.
- There is no endpoint to delete an uploaded photo.
- Refresh tokens are long-lived JWTs and can't be revoked server-side.
- Rate limiting is in-memory and per instance.
- Email verification is not implemented (the `is_verified` column exists).
- When a claim is approved, the claimant's own `LOST` report is not closed automatically.
- Admin gating in the UI is a convenience only. Real enforcement is `@PreAuthorize` on the server.

## Roadmap

- Let the finder review claims on their own items, with admins as fallback
- Auto-close the claimant's matching lost report when a claim is approved
- Semantic matching with pgvector embeddings, and image similarity
- WebSocket or push notifications
- Revocable refresh tokens
- Integration tests with Testcontainers