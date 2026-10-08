import { Link } from "react-router-dom";
import { useDocumentTitle } from "../hooks/useDocumentTitle";

export default function NotFoundPage() {
  useDocumentTitle("Page not found");

  return (
    <div className="empty-state full-page-empty">
      <h1 className="empty-state-title">Page not found</h1>
      <p className="empty-state-text">The page you're looking for doesn't exist or was moved.</p>
      <Link to="/" className="btn btn-primary">
        Go to dashboard
      </Link>
    </div>
  );
}
