import { NavLink } from "react-router-dom";
import { NAV_ITEMS } from "../utils/navConfig";
import { useAuth } from "../context/AuthContext";
import Icon from "./Icon";

export default function Sidebar({ onNavigate }) {
  const { isAdmin } = useAuth();
  const visibleItems = NAV_ITEMS.filter((item) => !item.adminOnly || isAdmin);

  return (
    <nav className="sidebar-nav" aria-label="Main navigation">
      <ul>
        {visibleItems.map((item) => (
          <li key={item.path}>
            <NavLink
              to={item.path}
              end={item.path === "/"}
              className={({ isActive }) => `sidebar-link ${isActive ? "sidebar-link-active" : ""}`}
              onClick={onNavigate}
            >
              <Icon name={item.icon} />
              <span>{item.label}</span>
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
