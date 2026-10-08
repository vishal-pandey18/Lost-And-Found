import api from "./api";

export function register({ name, email, password, phone }) {
  return api.post("/api/v1/auth/register", { name, email, password, phone });
}

export function login({ email, password }) {
  return api.post("/api/v1/auth/login", { email, password });
}

export function refresh(refreshToken) {
  return api.post("/api/v1/auth/refresh", { refreshToken });
}
