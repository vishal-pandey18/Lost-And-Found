import { useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import MobileDrawer from "../components/MobileDrawer";
import Breadcrumbs from "../components/Breadcrumbs";

export default function AppLayout() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const location = useLocation();
  const showBreadcrumbs = location.pathname !== "/";

  return (
    <div className="app-shell">
      <Topbar onMenuToggle={() => setIsDrawerOpen(true)} />

      <div className="app-body">
        <aside className="desktop-sidebar">
          <Sidebar />
        </aside>

        <MobileDrawer isOpen={isDrawerOpen} onClose={() => setIsDrawerOpen(false)} />

        <main className="app-main">
          {showBreadcrumbs && <Breadcrumbs />}
          <Outlet />
        </main>
      </div>
    </div>
  );
}
