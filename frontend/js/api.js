// Base URL of the Spring Boot backend. Change this if you run it elsewhere.
const API_BASE = "http://localhost:8080";

/**
 * Wraps fetch for JSON APIs. Throws an Error with the backend's message
 * on non-2xx responses so callers can just try/catch.
 */
async function apiFetch(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: options.body instanceof FormData ? {} : { "Content-Type": "application/json" },
    ...options,
  });

  // 204 No Content - nothing to parse
  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const message = (data && data.message) || `Request failed (${res.status})`;
    throw new Error(message);
  }

  return data;
}

const api = {
  register: (payload) =>
    apiFetch("/register", { method: "POST", body: JSON.stringify(payload) }),

  login: (payload) =>
    apiFetch("/login", { method: "POST", body: JSON.stringify(payload) }),

  getProfile: (id) => apiFetch(`/profile/${id}`),

  updateProfile: (id, payload) =>
    apiFetch(`/profile/${id}`, { method: "PUT", body: JSON.stringify(payload) }),

  createLostItem: (formData) =>
    apiFetch("/lost", { method: "POST", body: formData }),

  getLostItems: () => apiFetch("/lost"),

  markLostResolved: (id, userId) =>
    apiFetch(`/lost/${id}/resolve?userId=${userId}`, { method: "PATCH" }),

  deleteLostItem: (id, userId) =>
    apiFetch(`/lost/${id}?userId=${userId}`, { method: "DELETE" }),

  createFoundItem: (formData) =>
    apiFetch("/found", { method: "POST", body: formData }),

  getFoundItems: () => apiFetch("/found"),

  markFoundReturned: (id, userId) =>
    apiFetch(`/found/${id}/return?userId=${userId}`, { method: "PATCH" }),

  deleteFoundItem: (id, userId) =>
    apiFetch(`/found/${id}?userId=${userId}`, { method: "DELETE" }),

  search: (params) => {
    const query = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v))
    ).toString();
    return apiFetch(`/search${query ? `?${query}` : ""}`);
  },

  sendMessage: (payload) =>
    apiFetch("/message", { method: "POST", body: JSON.stringify(payload) }),

  getMessages: (userId) => apiFetch(`/messages/${userId}`),

  adminGetUsers: () => apiFetch("/admin/users"),
  adminDeleteUser: (id) => apiFetch(`/admin/users/${id}`, { method: "DELETE" }),
  adminDeleteLostItem: (id) => apiFetch(`/admin/lost/${id}`, { method: "DELETE" }),
  adminDeleteFoundItem: (id) => apiFetch(`/admin/found/${id}`, { method: "DELETE" }),
  adminGetDashboard: () => apiFetch("/admin/dashboard"),

  imageUrl: (path) => (path ? `${API_BASE}${path}` : null),
};
