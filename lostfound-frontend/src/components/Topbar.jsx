import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useUnreadCount } from "../hooks/useUnreadCount";
import Icon from "./Icon";

export default function Topbar({ onMenuToggle }) {
  const { logout, user } = useAuth();
  const { count } = useUnreadCount();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <header className="topbar">
      <button className="topbar-menu-btn" onClick={onMenuToggle} aria-label="Toggle menu">
        <Icon name="menu" />
      </button>

      <span className="topbar-brand">Lost &amp; Found</span>

      <div className="topbar-right">
        <Link to="/notifications" className="topbar-bell" aria-label="Notifications">
          <Icon name="bell" size={20} />
          {!!count && <span className="topbar-bell-badge">{count > 9 ? "9+" : count}</span>}
        </Link>
        {user?.email && <span className="topbar-user">{user.email}</span>}
        <button className="topbar-logout" onClick={handleLogout}>
          <Icon name="logout" size={18} />
          <span>Log out</span>
        </button>
      </div>
    </header>
  );
}
