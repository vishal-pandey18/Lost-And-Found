import { useAuth } from "../context/AuthContext";

// Assumes it's rendered inside ProtectedRoute already (so the user is
// definitely authenticated) - this only adds the role check on top.
export default function AdminRoute({ children }) {
  const { isAdmin } = useAuth();

  if (!isAdmin) {
    return (
      <div className="empty-state">
        <p className="empty-state-title">Admins only</p>
        <p className="empty-state-text">You don't have permission to view this page.</p>
      </div>
    );
  }

  return children;
}
