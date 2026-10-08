import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { getItemById, deleteItem } from "../services/itemService";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { useBreadcrumbLabel } from "../context/BreadcrumbContext";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { getErrorMessage } from "../utils/errorMessage";
import Badge from "../components/Badge";
import Button from "../components/Button";
import Spinner from "../components/Spinner";
import ConfirmModal from "../components/ConfirmModal";
import ImageUploader from "../components/ImageUploader";
import ClaimModal from "../components/ClaimModal";
import Icon from "../components/Icon";

export default function ItemDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isClaimModalOpen, setIsClaimModalOpen] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    setLoadError(null);
    getItemById(id)
      .then(({ data }) => setItem(data))
      .catch((err) => setLoadError(getErrorMessage(err)))
      .finally(() => setIsLoading(false));
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  useBreadcrumbLabel(`/items/${id}`, item?.title);
  useDocumentTitle(item?.title || "Item");

  const isOwner = user && item && item.reportedById === user.id;
  const canClaim =
    item &&
    !isOwner &&
    item.itemType === "FOUND" &&
    (item.status === "OPEN" || item.status === "CLAIMED");

  function handleClaimed() {
    setIsClaimModalOpen(false);
    setItem((prev) => (prev.status === "OPEN" ? { ...prev, status: "CLAIMED" } : prev));
  }

  async function handleDelete() {
    setIsDeleting(true);
    try {
      await deleteItem(id);
      toast.success("Item deleted.");
      navigate("/items");
    } catch (err) {
      toast.error(getErrorMessage(err));
      setIsDeleting(false);
      setIsDeleteModalOpen(false);
    }
  }

  function handleImageUploaded(newImage) {
    setItem((prev) => ({ ...prev, images: [...prev.images, newImage] }));
    toast.success("Photo uploaded.");
  }

  if (isLoading) {
    return (
      <div className="page-loading">
        <Spinner />
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="empty-state">
        <p className="empty-state-title">Couldn't load this item</p>
        <p className="empty-state-text">{loadError}</p>
        <Button variant="secondary" onClick={load}>
          Retry
        </Button>
      </div>
    );
  }

  if (!item) return null;

  return (
    <div className="page">
      <div className="item-detail-header">
        <div className="item-detail-badges">
          <Badge value={item.itemType} />
          <Badge value={item.status} />
        </div>
        {isOwner && (
          <div className="item-detail-owner-actions">
            <Link to={`/items/${id}/edit`}>
              <Button variant="secondary">
                <Icon name="edit" size={16} /> Edit
              </Button>
            </Link>
            <Button variant="danger" onClick={() => setIsDeleteModalOpen(true)}>
              <Icon name="trash" size={16} /> Delete
            </Button>
          </div>
        )}
        {canClaim && (
          <Button variant="primary" onClick={() => setIsClaimModalOpen(true)}>
            Claim this item
          </Button>
        )}
      </div>

      <h1 className="page-title">{item.title}</h1>
      <p className="page-subtitle">
        {item.category?.name}
        {item.location ? ` · ${item.location}` : ""}
        {item.eventDate ? ` · ${item.eventDate}` : ""}
      </p>

      {item.images?.length > 0 && (
        <div className="item-gallery">
          {item.images.map((img) => (
            <img key={img.id} src={img.imageUrl} alt={item.title} className="item-gallery-img" />
          ))}
        </div>
      )}

      {item.description && (
        <div className="card item-description-card">
          <p>{item.description}</p>
        </div>
      )}

      <div className="card item-meta-card">
        <p>
          <strong>Reported by:</strong> {item.reportedByName}
        </p>
        <p>
          <strong>Posted:</strong> {new Date(item.createdAt).toLocaleDateString()}
        </p>
      </div>

      {isOwner && (
        <div className="item-upload-section">
          <h2 className="section-title">Add photos</h2>
          <ImageUploader itemId={id} onUploaded={handleImageUploaded} />
        </div>
      )}

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        title="Delete this item?"
        message="This can't be undone from here. The item will be removed from the browse feed."
        confirmLabel="Delete"
        isLoading={isDeleting}
        onConfirm={handleDelete}
        onCancel={() => setIsDeleteModalOpen(false)}
      />

      <ClaimModal
        isOpen={isClaimModalOpen}
        itemId={id}
        onClose={() => setIsClaimModalOpen(false)}
        onClaimed={handleClaimed}
      />
    </div>
  );
}
