import { useState } from "react";
import { createClaim } from "../services/claimService";
import { useToast } from "../context/ToastContext";
import { validateClaimForm } from "../utils/validation";
import { getErrorMessage } from "../utils/errorMessage";
import Button from "./Button";

export default function ClaimModal({ isOpen, itemId, onClose, onClaimed }) {
  const toast = useToast();
  const [proofDescription, setProofDescription] = useState("");
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  async function handleSubmit(e) {
    e.preventDefault();
    const errors = validateClaimForm({ proofDescription });
    setError(errors.proofDescription || null);
    if (errors.proofDescription) return;

    setIsSubmitting(true);
    try {
      const { data } = await createClaim(itemId, proofDescription.trim());
      toast.success("Claim submitted. An admin will review it.");
      onClaimed?.(data);
      setProofDescription("");
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-panel"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        <h2 className="modal-title">Claim this item</h2>
        <p className="modal-message">
          Describe something specific about it that would prove it's yours - a mark, an initial,
          what was inside it, anything the finder wouldn't otherwise know.
        </p>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <textarea
              className={`form-input ${error ? "form-input-error" : ""}`}
              rows={4}
              value={proofDescription}
              onChange={(e) => setProofDescription(e.target.value)}
              placeholder="e.g. It has a small scratch on the back and my initials engraved inside."
              autoFocus
            />
            {error && <span className="form-error-text">{error}</span>}
          </div>

          <div className="modal-actions">
            <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={isSubmitting}>
              Submit claim
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
