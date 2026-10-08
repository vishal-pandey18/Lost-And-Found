import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { getMyClaims } from "../services/claimService";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { getErrorMessage } from "../utils/errorMessage";
import Badge from "../components/Badge";
import Spinner from "../components/Spinner";
import Button from "../components/Button";
import Pagination from "../components/Pagination";

export default function MyClaimsPage() {
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  useDocumentTitle("My Claims");

  const load = useCallback(() => {
    setIsLoading(true);
    setLoadError(null);
    getMyClaims(pageNumber)
      .then(({ data }) => setPage(data))
      .catch((err) => setLoadError(getErrorMessage(err)))
      .finally(() => setIsLoading(false));
  }, [pageNumber]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div className="page">
      <h1 className="page-title">My Claims</h1>
      <p className="page-subtitle">Items you've claimed and their review status.</p>

      {isLoading && (
        <div className="page-loading">
          <Spinner />
        </div>
      )}

      {!isLoading && loadError && (
        <div className="empty-state">
          <p className="empty-state-title">Couldn't load your claims</p>
          <p className="empty-state-text">{loadError}</p>
          <Button variant="secondary" onClick={load}>
            Retry
          </Button>
        </div>
      )}

      {!isLoading && !loadError && page?.content.length === 0 && (
        <div className="empty-state">
          <p className="empty-state-title">No claims yet</p>
          <p className="empty-state-text">
            When you claim a found item, it'll show up here with its review status.
          </p>
        </div>
      )}

      {!isLoading && !loadError && page?.content.length > 0 && (
        <>
          <div className="claim-list">
            {page.content.map((claim) => (
              <Link key={claim.id} to={`/items/${claim.itemId}`} className="claim-row">
                <div className="claim-row-main">
                  <span className="claim-item-title">{claim.itemTitle}</span>
                  <span className="claim-row-meta">
                    Submitted {new Date(claim.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <Badge value={claim.status} />
              </Link>
            ))}
          </div>
          <Pagination page={page} onPageChange={setPageNumber} />
        </>
      )}
    </div>
  );
}
