# Lost & Found Portal — Frontend

Plain HTML/CSS/JavaScript (no build step, no frameworks) that talks to the
Spring Boot backend via `fetch`. Theme: a community corkboard — reports look
like index cards pinned to a board, with a rotated ink-stamp badge for status.

## Running it

The backend must be running at `http://localhost:8080` first (see the
backend README). Then serve this folder with any static file server —
opening the files directly via `file://` will hit CORS/fetch restrictions
in most browsers, so use one of:

```bash
# Option A: Python
cd frontend
python3 -m http.server 5500

# Option B: Node
npx serve frontend -l 5500
```

Then visit `http://localhost:5500`.

If your backend runs somewhere other than `localhost:8080`, update
`API_BASE` at the top of `js/api.js`.

## Pages

| File | Purpose |
|---|---|
| `index.html` | Home — quick search + recently pinned reports |
| `login.html` / `register.html` | Auth |
| `dashboard.html` | Tabs for your lost items, found items, and messages |
| `report-lost.html` / `report-found.html` | Report forms with photo upload |
| `search.html` | Full filter search (keyword, category, color, location, date, type) |
| `profile.html` | View/update your account |
| `admin.html` | Stats + user management (requires an ADMIN-role account) |

## Becoming an admin

There's no self-service admin signup. After registering normally, promote
the account directly in MySQL:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

Then log out and back in so the frontend picks up the new role.

## Notes

- Session handling is just `localStorage` (see `js/auth.js`) — there's no
  JWT yet, matching the backend's current state. Don't treat this as secure
  auth; it's meant to be replaced once Spring Security/JWT is added.
- `js/items.js` normalizes lost items, found items, and search results into
  one shared shape so `renderItemCard()` can render all three consistently.
- Editing an existing report isn't wired up in the UI yet (delete + mark
  resolved/returned are) — the `PUT /lost/{id}` and `PUT /found/{id}`
  endpoints are ready on the backend whenever you want to add an edit form.
