import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useUnreadCount } from "../hooks/useUnreadCount";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import Spinner from "../components/Spinner";

export default function DashboardPage() {
  const { user } = useAuth();
  const { count, hasError } = useUnreadCount();

  useDocumentTitle("Dashboard");

  return (
    <div className="page">
      <h1 className="page-title">Welcome{user?.email ? `, ${user.email}` : ""}</h1>
      <p className="page-subtitle">Here's what's happening with your account.</p>

      <div className="card-grid">
        <Link to="/notifications" className="card stat-card stat-card-link">
          <span className="stat-label">Unread notifications</span>
          {count === null && !hasError && <Spinner size="sm" />}
          {count === null && hasError && <span className="stat-error">Couldn't load</span>}
          {count !== null && <span className="stat-value">{count}</span>}
        </Link>
      </div>
    </div>
  );
}
