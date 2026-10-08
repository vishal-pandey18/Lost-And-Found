import { Link, useLocation } from "react-router-dom";
import { useBreadcrumbLabels } from "../context/BreadcrumbContext";

// Turns "/items/report" into Home > Items > Report by title-casing each raw
// segment - except where a page has registered a real label (an item's
// actual title instead of its UUID) via useBreadcrumbLabel, which always wins.
export default function Breadcrumbs() {
  const location = useLocation();
  const registeredLabels = useBreadcrumbLabels();
  const segments = location.pathname.split("/").filter(Boolean);

  if (segments.length === 0) return null;

  const crumbs = segments.map((segment, index) => {
    const path = "/" + segments.slice(0, index + 1).join("/");
    const fallbackLabel = segment
      .replace(/-/g, " ")
      .replace(/\b\w/g, (c) => c.toUpperCase());
    return { path, label: registeredLabels[path] || fallbackLabel };
  });

  return (
    <nav className="breadcrumbs" aria-label="Breadcrumb">
      <Link to="/">Home</Link>
      {crumbs.map((crumb, i) => (
        <span key={crumb.path}>
          <span className="breadcrumb-sep">/</span>
          {i === crumbs.length - 1 ? (
            <span className="breadcrumb-current">{crumb.label}</span>
          ) : (
            <Link to={crumb.path}>{crumb.label}</Link>
          )}
        </span>
      ))}
    </nav>
  );
}
