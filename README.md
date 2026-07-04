# Lost & Found Portal — Backend

Full Spring Boot + MySQL backend covering Phases 1-7 of the roadmap
(user management, lost items, found items, search, messaging, admin panel).

## 1. Prerequisites

- Java 17+
- Maven
- MySQL running locally (or update the connection env vars below)

## 2. Configure environment variables

```bash
export DB_URL="jdbc:mysql://localhost:3306/lost_and_found?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export UPLOAD_DIR=uploads
```

`createDatabaseIfNotExist=true` means you don't need to manually create the
schema — Hibernate (`ddl-auto=update`) will create/update all tables on startup.

## 3. Run it

```bash
mvn spring-boot:run
```

Server starts on `http://localhost:8080`.

## 4. Authentication note (read this)

There's no JWT/session yet — that's listed as a **future enhancement** in
the roadmap, so it's intentionally left out of this pass. `/login` just
verifies the password and returns the user's info. The frontend should hold
onto the returned `id` (e.g. in memory or localStorage) and pass it as
`userId` on requests that need to know who's acting (creating/editing/
deleting items, sending messages).

This means right now nothing stops user A from passing user B's `userId` —
fine for local dev, **not fine for production**. Add Spring Security + JWT
before deploying this anywhere real.

## 5. API Reference

### Auth
| Method | Endpoint | Body |
|---|---|---|
| POST | `/register` | `{name, email, password, phone}` |
| POST | `/login` | `{email, password}` |

### Profile
| Method | Endpoint | Body |
|---|---|---|
| GET | `/profile/{id}` | — |
| PUT | `/profile/{id}` | `{name, email, phone, newPassword}` (all optional) |

### Lost Items
All write endpoints are `multipart/form-data` (to support image upload).
| Method | Endpoint | Params |
|---|---|---|
| POST | `/lost` | `userId` (query) + form fields `title, description, category, color, location, lostDate` + optional `image` file |
| GET | `/lost` | — |
| GET | `/lost/{id}` | — |
| PUT | `/lost/{id}` | `userId` (query) + same form fields |
| DELETE | `/lost/{id}` | `userId` (query) |
| PATCH | `/lost/{id}/resolve` | `userId` (query) — marks item as `RESOLVED` |

### Found Items
Same shape as Lost Items, swap `lostDate` → `foundDate`.
| Method | Endpoint |
|---|---|
| POST | `/found` |
| GET | `/found` |
| GET | `/found/{id}` |
| PUT | `/found/{id}` |
| DELETE | `/found/{id}` |
| PATCH | `/found/{id}/return` — marks item as `RETURNED` |

### Search
| Method | Endpoint | Query params (all optional) |
|---|---|---|
| GET | `/search` | `keyword, category, color, location, date (yyyy-MM-dd), status, type (LOST/FOUND/ALL)` |

Returns a unified list combining lost + found reports, newest first, so the
UI can render both in one results grid.

### Messaging (contact reporter)
| Method | Endpoint | Body |
|---|---|---|
| POST | `/message` | `{senderId, receiverId, message}` |
| GET | `/messages/{userId}` | — (sent + received, newest first) |

### Admin
| Method | Endpoint |
|---|---|
| GET | `/admin/users` |
| DELETE | `/admin/users/{id}` |
| DELETE | `/admin/lost/{id}` |
| DELETE | `/admin/found/{id}` |
| GET | `/admin/dashboard` — user/report/resolved counts |

## 6. Testing with curl

```bash
# Register
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Asha","email":"asha@example.com","password":"secret123","phone":"9999999999"}'

# Login
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"asha@example.com","password":"secret123"}'

# Report a lost item (userId=1 from the register response)
curl -X POST http://localhost:8080/lost \
  -F "userId=1" \
  -F "title=Black Wallet" \
  -F "category=Wallet" \
  -F "color=Black" \
  -F "location=Library" \
  -F "lostDate=2026-07-01" \
  -F "image=@/path/to/photo.jpg"

# Search
curl "http://localhost:8080/search?category=Wallet&location=Library"
```

## 7. Project structure

```
src/main/java/com/example/lostandfound/
├── LostAndFoundApplication.java
├── config/          # CORS, static file serving, password encoder
├── entity/          # User, LostItem, FoundItem, Message + enums
├── repository/       # Spring Data JPA repositories (+ Specifications for search)
├── dto/              # request/response objects, grouped by feature
├── service/           # business logic — one per module
├── controller/        # REST endpoints — matches the roadmap's API summary
└── exception/          # custom exceptions + global JSON error handler
```

## 8. Frontend

A full vanilla HTML/CSS/JS frontend now lives in `frontend/` — see
`frontend/README.md` for how to run it. It covers every page from the
roadmap (home, login, register, dashboard, report lost/found, search,
profile, admin) and talks to this backend over `fetch`.

## 9. What's not built yet (by design, per roadmap's "Future Enhancements")

- JWT authentication / Spring Security
- Email notifications
- Cloudinary image hosting (currently stores to local disk under `uploads/`)
- OCR / AI-based matching
- QR code verification
- WebSocket real-time chat
- An "edit report" UI on the frontend (the backend endpoints are ready)

Happy to add JWT auth, tests, or Docker/deployment config next — just say which.
