import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { getClaimsQueue, reviewClaim } from "../services/claimService";
import { useToast } from "../context/ToastContext";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { getErrorMessage } from "../utils/errorMessage";
import Badge from "../components/Badge";
import Spinner from "../components/Spinner";
import Button from "../components/Button";
import Pagination from "../components/Pagination";
import ConfirmModal from "../components/ConfirmModal";

const STATUS_TABS = ["PENDING", "APPROVED", "REJECTED"];

export default function AdminClaimsPage() {
  const toast = useToast();
  const [status, setStatus] = useState("PENDING");
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [pendingAction, setPendingAction] = useState(null); // { claimId, decision }
  const [isActing, setIsActing] = useState(false);

  useDocumentTitle("Claim Queue");

  const load = useCallback(() => {
    setIsLoading(true);
    setLoadError(null);
    getClaimsQueue(status, pageNumber)
      .then(({ data }) => setPage(data))
      .catch((err) => setLoadError(getErrorMessage(err)))
      .finally(() => setIsLoading(false));
  }, [status, pageNumber]);

  useEffect(() => {
    load();
  }, [load]);

  function handleTabChange(next) {
    setStatus(next);
    setPageNumber(0);
  }

  async function handleConfirmAction() {
    if (!pendingAction) return;
    setIsActing(true);
    try {
      await reviewClaim(pendingAction.claimId, pendingAction.decision);
      toast.success(pendingAction.decision === "APPROVED" ? "Claim approved." : "Claim rejected.");
      setPendingAction(null);
      load();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setIsActing(false);
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">Claim Moderation Queue</h1>
      <p className="page-subtitle">Review and decide on submitted claims.</p>

      <div className="status-tabs">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab}
            className={`status-tab ${status === tab ? "status-tab-active" : ""}`}
            onClick={() => handleTabChange(tab)}
          >
            {tab}
          </button>
        ))}
      </div>

      {isLoading && (
        <div className="page-loading">
          <Spinner />
        </div>
      )}

      {!isLoading && loadError && (
        <div className="empty-state">
          <p className="empty-state-title">Couldn't load claims</p>
          <p className="empty-state-text">{loadError}</p>
          <Button variant="secondary" onClick={load}>
            Retry
          </Button>
        </div>
      )}

      {!isLoading && !loadError && page?.content.length === 0 && (
        <div className="empty-state">
          <p className="empty-state-title">No {status.toLowerCase()} claims</p>
          <p className="empty-state-text">Nothing to review here right now.</p>
        </div>
      )}

      {!isLoading && !loadError && page?.content.length > 0 && (
        <>
          <div className="claim-list">
            {page.content.map((claim) => (
              <div key={claim.id} className="claim-row claim-row-admin">
                <div className="claim-row-main">
                  <Link to={`/items/${claim.itemId}`} className="claim-item-title">
                    {claim.itemTitle}
                  </Link>
                  <span className="claim-row-meta">
                    Claimed by {claim.claimedByName} on{" "}
                    {new Date(claim.createdAt).toLocaleDateString()}
                  </span>
                  <p className="claim-proof">{claim.proofDescription}</p>
                </div>

                <div className="claim-row-side">
                  <Badge value={claim.status} />
                  {claim.status === "PENDING" && (
                    <div className="claim-actions">
                      <Button
                        variant="secondary"
                        onClick={() => setPendingAction({ claimId: claim.id, decision: "REJECTED" })}
                      >
                        Reject
                      </Button>
                      <Button
                        variant="primary"
                        onClick={() => setPendingAction({ claimId: claim.id, decision: "APPROVED" })}
                      >
                        Approve
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
          <Pagination page={page} onPageChange={setPageNumber} />
        </>
      )}

      <ConfirmModal
        isOpen={!!pendingAction}
        title={pendingAction?.decision === "APPROVED" ? "Approve this claim?" : "Reject this claim?"}
        message={
          pendingAction?.decision === "APPROVED"
            ? "This resolves the item and automatically rejects any other pending claims on it."
            : "The claimant will be notified. If no other claims are pending, the item reopens."
        }
        confirmLabel={pendingAction?.decision === "APPROVED" ? "Approve" : "Reject"}
        isLoading={isActing}
        onConfirm={handleConfirmAction}
        onCancel={() => setPendingAction(null)}
      />
    </div>
  );
}
