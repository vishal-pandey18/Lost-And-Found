import { createContext, useContext, useState, useCallback, useEffect } from "react";

// Breadcrumbs derive labels from the URL by default ("/items/abc-123" -> "Abc 123"),
// which is fine for static routes but useless for a UUID. Pages with a real
// human-readable name (an item's title, say) call setLabel once the data
// loads, and Breadcrumbs prefers that over the auto-generated guess.
const BreadcrumbContext = createContext(null);

export function BreadcrumbProvider({ children }) {
  const [labels, setLabels] = useState({});

  const setLabel = useCallback((path, label) => {
    setLabels((prev) => (prev[path] === label ? prev : { ...prev, [path]: label }));
  }, []);

  return (
    <BreadcrumbContext.Provider value={{ labels, setLabel }}>{children}</BreadcrumbContext.Provider>
  );
}

export function useBreadcrumbLabel(path, label) {
  const ctx = useContext(BreadcrumbContext);
  useEffect(() => {
    if (ctx && label) {
      ctx.setLabel(path, label);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [path, label]);
}

export function useBreadcrumbLabels() {
  const ctx = useContext(BreadcrumbContext);
  if (!ctx) throw new Error("useBreadcrumbLabels must be used within a BreadcrumbProvider");
  return ctx.labels;
}
