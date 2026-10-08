import { useEffect, useState, useCallback } from "react";
import { getUnreadCount } from "../services/notificationService";
import { useAuth } from "../context/AuthContext";

const POLL_INTERVAL_MS = 30000;

// Polls periodically rather than on every render, and only while the user
// is actually authenticated - avoids hammering the API and avoids firing
// requests from a page that's about to redirect to /login anyway.
export function useUnreadCount() {
  const { isAuthenticated } = useAuth();
  const [count, setCount] = useState(null);
  const [hasError, setHasError] = useState(false);

  const refresh = useCallback(() => {
    if (!isAuthenticated) return;
    getUnreadCount()
      .then(({ data }) => {
        setCount(data.unreadCount);
        setHasError(false);
      })
      .catch(() => {
        // Keep the last known count showing (better UX than blanking it on
        // a transient failure), but surface the error so a caller with no
        // prior value yet can show a real error state instead of spinning forever.
        setHasError(true);
      });
  }, [isAuthenticated]);

  useEffect(() => {
    if (!isAuthenticated) {
      setCount(null);
      setHasError(false);
      return;
    }
    refresh();
    const interval = setInterval(refresh, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [isAuthenticated, refresh]);

  return { count, hasError, refresh };
}
