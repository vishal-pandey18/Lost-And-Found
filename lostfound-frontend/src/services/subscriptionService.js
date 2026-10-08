import api from "./api";

export function createSubscription({ categoryId, keyword }) {
  return api.post("/api/v1/subscriptions", { categoryId, keyword });
}

export function getSubscriptions() {
  return api.get("/api/v1/subscriptions");
}

export function deleteSubscription(id) {
  return api.delete(`/api/v1/subscriptions/${id}`);
}
