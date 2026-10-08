import Sidebar from "./Sidebar";
import Icon from "./Icon";

export default function MobileDrawer({ isOpen, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="drawer-overlay" onClick={onClose}>
      <div className="drawer-panel" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-header">
          <span className="topbar-brand">Lost &amp; Found</span>
          <button className="drawer-close-btn" onClick={onClose} aria-label="Close menu">
            <Icon name="close" />
          </button>
        </div>
        <Sidebar onNavigate={onClose} />
      </div>
    </div>
  );
}
