import api from "./api";

export function createClaim(itemId, proofDescription) {
  return api.post(`/api/v1/items/${itemId}/claims`, { proofDescription });
}

export function getMyClaims(page = 0, size = 10) {
  return api.get("/api/v1/claims/mine", { params: { page, size } });
}

export function getClaimById(id) {
  return api.get(`/api/v1/claims/${id}`);
}

export function getClaimsQueue(status, page = 0, size = 10) {
  return api.get("/api/v1/claims", { params: { status, page, size } });
}

export function reviewClaim(id, status) {
  return api.patch(`/api/v1/claims/${id}`, { status });
}
