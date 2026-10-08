import api from "./api";

export function getCategories() {
  return api.get("/api/v1/categories");
}
