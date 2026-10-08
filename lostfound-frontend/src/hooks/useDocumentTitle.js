import { useEffect } from "react";

export function useDocumentTitle(title) {
  useEffect(() => {
    const previous = document.title;
    document.title = title ? `${title} · Lost & Found` : "Lost & Found";
    return () => {
      document.title = previous;
    };
  }, [title]);
}
