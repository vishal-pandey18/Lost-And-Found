import { useEffect, useState } from "react";
import {
  createSubscription,
  getSubscriptions,
  deleteSubscription,
} from "../services/subscriptionService";
import { getCategories } from "../services/categoryService";
import { useToast } from "../context/ToastContext";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { validateSubscriptionForm } from "../utils/validation";
import { getErrorMessage } from "../utils/errorMessage";
import Button from "../components/Button";
import Spinner from "../components/Spinner";
import Icon from "../components/Icon";

export default function SubscriptionsPage() {
  const toast = useToast();
  const [categories, setCategories] = useState([]);
  const [subscriptions, setSubscriptions] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [categoryId, setCategoryId] = useState("");
  const [keyword, setKeyword] = useState("");
  const [formError, setFormError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [removingId, setRemovingId] = useState(null);

  useDocumentTitle("Alert Subscriptions");

  function load() {
    setIsLoading(true);
    setLoadError(null);
    getSubscriptions()
      .then(({ data }) => setSubscriptions(data))
      .catch((err) => setLoadError(getErrorMessage(err)))
      .finally(() => setIsLoading(false));
  }

  useEffect(() => {
    getCategories().then(({ data }) => setCategories(data));
    load();
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    const errors = validateSubscriptionForm({ categoryId, keyword });
    setFormError(errors.general || null);
    if (errors.general) return;

    setIsSubmitting(true);
    try {
      await createSubscription({
        categoryId: categoryId ? Number(categoryId) : undefined,
        keyword: keyword.trim() || undefined,
      });
      toast.success("Alert created.");
      setCategoryId("");
      setKeyword("");
      load();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleRemove(id) {
    setRemovingId(id);
    try {
      await deleteSubscription(id);
      setSubscriptions((prev) => prev.filter((s) => s.id !== id));
      toast.success("Alert removed.");
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setRemovingId(null);
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">Alert Subscriptions</h1>
      <p className="page-subtitle">
        Get notified when a found item matches a category or keyword you're watching for.
      </p>

      <form className="card form-card subscription-form" onSubmit={handleSubmit} noValidate>
        <div className="subscription-form-row">
          <div className="form-field">
            <label className="form-label">Category</label>
            <select
              className="form-input"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
            >
              <option value="">Any category</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-field">
            <label className="form-label">Keyword</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. umbrella"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
          </div>
        </div>
        {formError && <span className="form-error-text">{formError}</span>}
        <Button type="submit" loading={isSubmitting}>
          Add alert
        </Button>
      </form>

      <h2 className="section-title">Your alerts</h2>

      {isLoading && (
        <div className="page-loading">
          <Spinner />
        </div>
      )}

      {!isLoading && loadError && (
        <div className="empty-state">
          <p className="empty-state-title">Couldn't load your alerts</p>
          <p className="empty-state-text">{loadError}</p>
          <Button variant="secondary" onClick={load}>
            Retry
          </Button>
        </div>
      )}

      {!isLoading && !loadError && subscriptions?.length === 0 && (
        <div className="empty-state">
          <p className="empty-state-title">No alerts yet</p>
          <p className="empty-state-text">Add one above to get notified about matching items.</p>
        </div>
      )}

      {!isLoading && !loadError && subscriptions?.length > 0 && (
        <div className="subscription-list">
          {subscriptions.map((s) => (
            <div key={s.id} className="subscription-row">
              <span>
                {s.categoryName && <strong>{s.categoryName}</strong>}
                {s.categoryName && s.keyword ? " · " : ""}
                {s.keyword && `"${s.keyword}"`}
              </span>
              <button
                className="subscription-remove-btn"
                onClick={() => handleRemove(s.id)}
                disabled={removingId === s.id}
                aria-label="Remove alert"
              >
                <Icon name="trash" size={16} />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
