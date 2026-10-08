import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  getNotifications,
  markAsRead,
  markAllAsRead,
} from "../services/notificationService";
import { useToast } from "../context/ToastContext";
import { useUnreadCount } from "../hooks/useUnreadCount";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { getErrorMessage } from "../utils/errorMessage";
import Spinner from "../components/Spinner";
import Button from "../components/Button";
import Pagination from "../components/Pagination";

export default function NotificationsPage() {
  const toast = useToast();
  const { refresh: refreshBadge } = useUnreadCount();
  const navigate = useNavigate();

  const [unreadOnly, setUnreadOnly] = useState(false);
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [isMarkingAll, setIsMarkingAll] = useState(false);

  useDocumentTitle("Notifications");

  const load = useCallback(() => {
    setIsLoading(true);
    setLoadError(null);
    getNotifications({ unreadOnly, page: pageNumber })
      .then(({ data }) => setPage(data))
      .catch((err) => setLoadError(getErrorMessage(err)))
      .finally(() => setIsLoading(false));
  }, [unreadOnly, pageNumber]);

  useEffect(() => {
    load();
  }, [load]);

  function toggleUnreadOnly(value) {
    setUnreadOnly(value);
    setPageNumber(0);
  }

  async function handleNotificationClick(notification) {
    if (!notification.read) {
      try {
        await markAsRead(notification.id);
        setPage((prev) => ({
          ...prev,
          content: prev.content.map((n) =>
            n.id === notification.id ? { ...n, read: true } : n
          ),
        }));
        refreshBadge();
      } catch {
        // Non-critical - the notification still navigates even if marking
        // read failed; the user can retry by revisiting it.
      }
    }
    if (notification.itemId) {
      navigate(`/items/${notification.itemId}`);
    }
  }

  async function handleMarkAllRead() {
    setIsMarkingAll(true);
    try {
      await markAllAsRead();
      toast.success("All notifications marked as read.");
      refreshBadge();
      load();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setIsMarkingAll(false);
    }
  }

  return (
    <div className="page">
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">Matches for your lost items, and updates on your claims.</p>
        </div>
        <Button variant="secondary" onClick={handleMarkAllRead} loading={isMarkingAll}>
          Mark all as read
        </Button>
      </div>

      <div className="status-tabs">
        <button
          className={`status-tab ${!unreadOnly ? "status-tab-active" : ""}`}
          onClick={() => toggleUnreadOnly(false)}
        >
          All
        </button>
        <button
          className={`status-tab ${unreadOnly ? "status-tab-active" : ""}`}
          onClick={() => toggleUnreadOnly(true)}
        >
          Unread
        </button>
      </div>

      {isLoading && (
        <div className="page-loading">
          <Spinner />
        </div>
      )}

      {!isLoading && loadError && (
        <div className="empty-state">
          <p className="empty-state-title">Couldn't load notifications</p>
          <p className="empty-state-text">{loadError}</p>
          <Button variant="secondary" onClick={load}>
            Retry
          </Button>
        </div>
      )}

      {!isLoading && !loadError && page?.content.length === 0 && (
        <div className="empty-state">
          <p className="empty-state-title">
            {unreadOnly ? "No unread notifications" : "No notifications yet"}
          </p>
          <p className="empty-state-text">
            You'll see updates here when items match what you've lost, or when someone acts on
            your claims.
          </p>
        </div>
      )}

      {!isLoading && !loadError && page?.content.length > 0 && (
        <>
          <div className="notification-list">
            {page.content.map((n) => (
              <button
                key={n.id}
                className={`notification-row ${!n.read ? "notification-row-unread" : ""}`}
                onClick={() => handleNotificationClick(n)}
              >
                {!n.read && <span className="notification-dot" aria-hidden="true" />}
                <div className="notification-row-body">
                  <span className="notification-title">{n.title}</span>
                  <span className="notification-message">{n.message}</span>
                  <span className="notification-time">
                    {new Date(n.createdAt).toLocaleString()}
                  </span>
                </div>
              </button>
            ))}
          </div>
          <Pagination page={page} onPageChange={setPageNumber} />
        </>
      )}
    </div>
  );
}
