// Works directly with a Spring Data Page object shape:
// { number, totalPages, totalElements, size }
export default function Pagination({ page, onPageChange }) {
  if (!page || page.totalPages <= 1) return null;

  const current = page.number;
  const last = page.totalPages - 1;

  return (
    <nav className="pagination" aria-label="Pagination">
      <button
        className="btn btn-secondary"
        disabled={current === 0}
        onClick={() => onPageChange(current - 1)}
      >
        Previous
      </button>
      <span className="pagination-info">
        Page {current + 1} of {page.totalPages} &middot; {page.totalElements} total
      </span>
      <button
        className="btn btn-secondary"
        disabled={current === last}
        onClick={() => onPageChange(current + 1)}
      >
        Next
      </button>
    </nav>
  );
}
