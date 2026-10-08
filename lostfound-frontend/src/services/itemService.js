import api from "./api";

export function searchItems({ itemType, status, categoryId, page = 0, size = 12 } = {}) {
  return api.get("/api/v1/items", {
    params: { itemType, status, categoryId, page, size },
  });
}

export function getItemById(id) {
  return api.get(`/api/v1/items/${id}`);
}

export function createItem(payload) {
  return api.post("/api/v1/items", payload);
}

export function updateItem(id, payload) {
  return api.patch(`/api/v1/items/${id}`, payload);
}

export function deleteItem(id) {
  return api.delete(`/api/v1/items/${id}`);
}

export function uploadItemImage(id, file, onUploadProgress) {
  const formData = new FormData();
  formData.append("file", file);
  return api.post(`/api/v1/items/${id}/images`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress,
  });
}
