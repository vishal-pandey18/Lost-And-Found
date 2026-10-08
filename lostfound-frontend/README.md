# Lost & Found Frontend

React + Vite frontend for the Lost & Found backend.

## Status: All 5 phases complete

| Phase | Scope | Status |
|---|---|---|
| 1 | Project setup, full auth (login/register/refresh), protected routes, layout shell (sidebar/topbar/mobile drawer), toast system, error handling | Done |
| 2 | Categories + Items: browse feed (filter/paginate/client-side search), item detail, create/edit form, delete with confirm, drag-drop image upload with progress | Done |
| 3 | Claims: claim-this-item modal, my claims history, admin moderation queue with approve/reject, role-gated nav and routes | Done |
| 4 | Notifications: live-polling unread badge in the topbar, full inbox with mark-read/mark-all-read; Alert Subscriptions: create/list/remove category+keyword watches | Done |
| **5** | **Polish: real breadcrumb labels (item titles instead of raw UUIDs), per-page document titles, defensive responsive fixes across modals/tabs/rows for narrow screens** | **Done** |

### Backend change required for Phase 3

The backend's JWT now includes a `role` claim (`USER` or `ADMIN`), added specifically so this frontend can show/hide the admin moderation queue without an extra API call. **You need the updated backend zip** (`lostfound-complete.zip`, regenerated alongside this) — the old backend will still work for login/register, but tokens it issues won't carry `role`, so `isAdmin` will always evaluate to `false` until you're running the updated backend.

This mirrors how the backend was built — in verified phases, each one actually running, rather than one large unverifiable dump.

## Setup

```bash
npm install
cp .env.example .env   # edit VITE_API_BASE_URL if your backend isn't on localhost:8081
npm run dev
```

Your Spring Boot backend must be running (see the backend README) and its CORS config must allow this frontend's origin — it currently allows `*` for development, so this works out of the box locally.

## What's actually wired up right now

- **Register** → `POST /api/v1/auth/register`
- **Login** → `POST /api/v1/auth/login`
- **Automatic token refresh** → `POST /api/v1/auth/refresh`, triggered transparently by an axios interceptor on any 401
- **Dashboard** → `GET /api/v1/notifications/unread-count` (real data, not a placeholder)
- **Logout** → clears tokens, redirects to login
- **Browse items** → `GET /api/v1/items` with `itemType`/`status`/`categoryId`/`page`/`size`, real pagination
- **Categories filter** → `GET /api/v1/categories`
- **Item detail** → `GET /api/v1/items/{id}`
- **Report an item** → `POST /api/v1/items`
- **Edit an item** (owner only) → `PATCH /api/v1/items/{id}`, prefilled from the real record
- **Delete an item** (owner only) → `DELETE /api/v1/items/{id}`, behind a confirmation modal
- **Upload photos** (owner only) → `POST /api/v1/items/{id}/images`, drag-drop or browse, real per-file progress bars, multiple files

- **Claim an item** → `POST /api/v1/items/{itemId}/claims`, via a modal on the item detail page (shown only for FOUND items, not OPEN/CLAIMED, not your own)
- **My claims** → `GET /api/v1/claims/mine`, paginated
- **Admin moderation queue** (admin only) → `GET /api/v1/claims?status=`, tabbed by PENDING/APPROVED/REJECTED
- **Approve/reject a claim** (admin only) → `PATCH /api/v1/claims/{id}`, behind a confirmation modal explaining the consequence (auto-resolves the item / auto-rejects competing claims)

- **Live unread badge** → `GET /api/v1/notifications/unread-count`, polled every 30s via `useUnreadCount`, shown on the topbar bell and the dashboard
- **Notification inbox** → `GET /api/v1/notifications?unreadOnly=`, paginated, tabbed All/Unread
- **Mark one as read** → `PATCH /api/v1/notifications/{id}/read`, triggered by clicking a notification (which also navigates to the related item, if any)
- **Mark all as read** → `PATCH /api/v1/notifications/read-all`
- **Create an alert** → `POST /api/v1/subscriptions` (category, keyword, or both)
- **List/remove alerts** → `GET` / `DELETE /api/v1/subscriptions/{id}`

- **Real breadcrumbs** — `/items/{id}` and `/items/{id}/edit` now show the item's actual title (registered via `useBreadcrumbLabel` once the item loads), not the raw UUID from the URL
- **Document titles** — every page sets a real browser tab title (e.g. "My Claims · Lost & Found"), including the item's own title on its detail/edit pages

## Known limitations (Phase 5)

- **Responsive fixes are defensive, not visually verified in a real browser.** This sandbox has no way to actually render and screenshot the app at different viewport widths, so the Phase 5 CSS additions (flex-wrap on modals/tabs/rows, a narrow-screen layout tweak on the item detail header) are reasoned fixes based on the same patterns used successfully throughout every earlier phase — relative units, `flex-wrap`, `minmax()` grids — not a pixel-verified QA pass. Worth an honest 10-minute look in your own browser's device toolbar before considering this fully done.

## Known limitations (Phase 4)

- **Polling, not push.** The unread badge refreshes every 30 seconds, not instantly via WebSocket. This was a deliberate simplicity trade-off flagged back when the notification system was first designed on the backend — real-time push is a valid upgrade path (Phase 6+ in the original roadmap), not an oversight here.

## Known limitations (Phase 3)

- **Admin gating is a UI convenience, not a security boundary** — hiding the nav link and route content client-side is for user experience only. The real enforcement is server-side (`@PreAuthorize("hasRole('ADMIN')")` on the backend), exactly as it should be — a non-admin who somehow reached the queue page would still get a 403 from every actual API call.

## Known limitations (Phase 2)

- **No server-side full-text search.** The backend only filters by `itemType`/`status`/`categoryId` - there's no `?search=` endpoint. The search box on the items list filters the *currently loaded page* client-side rather than faking a server search that doesn't exist. A real fix would be a backend text-search endpoint (or the pgvector semantic search discussed earlier).
- **No "remove uploaded photo" option.** The backend has no delete-image endpoint, only add. You can remove a file from the upload queue *before* it finishes uploading, but not after - this is a backend gap, not a frontend oversight.
- **Latitude/longitude aren't in the form.** The backend accepts them, but a manual lat/long text field isn't a realistic way for a person to enter location data - a map picker would be the real fix, deferred rather than faked with a plain number input.
- **Protected routes** → redirect to `/login` if not authenticated, then return you to where you were headed after logging in

## Architecture notes

- **Tokens live in localStorage.** Simpler for a project this size than in-memory storage; the trade-off (slightly higher XSS exposure) is worth knowing if this ever goes to production for real.
- **User identity is read by decoding the JWT client-side** (`utils/jwt.js`), not from a `/me` endpoint — the backend doesn't expose one. This is safe for *display* purposes since the backend independently verifies every request; the frontend decode is never used for authorization decisions.
- **No `role` claim in the JWT yet.** The backend's token only carries email + user id. Phase 3 (Admin moderation) will need either a `role` claim added to the JWT or a small `GET /users/me` endpoint — flagged now rather than faking admin detection client-side.
- **`services/api.js`** is the single axios instance everything uses — token attachment and refresh-on-401 both happen here, once, rather than being repeated per API call.
- **`utils/navConfig.js`** is the single source of truth for sidebar/drawer links — add a route here in the same phase you build its page, so nav never points at something that doesn't exist yet.

## Verified working

- `npm run build` — clean, 124 modules, no errors
- `npm run dev` — starts cleanly on port 5173
