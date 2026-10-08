import api from "./api";

export function getUnreadCount() {
  return api.get("/api/v1/notifications/unread-count");
}

export function getNotifications({ unreadOnly = false, page = 0, size = 15 } = {}) {
  return api.get("/api/v1/notifications", { params: { unreadOnly, page, size } });
}

export function markAsRead(id) {
  return api.patch(`/api/v1/notifications/${id}/read`);
}

export function markAllAsRead() {
  return api.patch("/api/v1/notifications/read-all");
}
