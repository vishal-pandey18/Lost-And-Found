import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getItemById, createItem, updateItem } from "../services/itemService";
import { getCategories } from "../services/categoryService";
import { useToast } from "../context/ToastContext";
import { useBreadcrumbLabel } from "../context/BreadcrumbContext";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { validateItemForm } from "../utils/validation";
import { getErrorMessage } from "../utils/errorMessage";
import FormField from "../components/FormField";
import Button from "../components/Button";
import Spinner from "../components/Spinner";

const EMPTY_FORM = {
  title: "",
  description: "",
  categoryId: "",
  itemType: "LOST",
  status: "OPEN",
  location: "",
  eventDate: "",
};

export default function ItemFormPage() {
  const { id } = useParams();
  const isEditMode = Boolean(id);
  const navigate = useNavigate();
  const toast = useToast();

  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(isEditMode);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // In edit mode, the breadcrumb trail is Home > Items > {title} > Edit -
  // registering the title here means the {title} segment shows the real
  // name instead of the raw UUID, same as the detail page.
  useBreadcrumbLabel(`/items/${id}`, isEditMode ? form.title : undefined);
  useDocumentTitle(isEditMode ? `Edit ${form.title || "item"}` : "Report an item");

  useEffect(() => {
    getCategories().then(({ data }) => setCategories(data));
  }, []);

  useEffect(() => {
    if (!isEditMode) return;
    getItemById(id)
      .then(({ data }) => {
        setForm({
          title: data.title,
          description: data.description || "",
          categoryId: data.category.id,
          itemType: data.itemType,
          status: data.status,
          location: data.location || "",
          eventDate: data.eventDate || "",
        });
      })
      .finally(() => setIsLoading(false));
  }, [id, isEditMode]);

  function handleChange(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const validationErrors = validateItemForm({ ...form, isCreate: !isEditMode });
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    setIsSubmitting(true);
    try {
      if (isEditMode) {
        await updateItem(id, {
          title: form.title,
          description: form.description,
          categoryId: Number(form.categoryId),
          status: form.status,
          location: form.location || null,
          eventDate: form.eventDate || null,
        });
        toast.success("Item updated.");
        navigate(`/items/${id}`);
      } else {
        const { data } = await createItem({
          title: form.title,
          description: form.description,
          categoryId: Number(form.categoryId),
          itemType: form.itemType,
          location: form.location || null,
          eventDate: form.eventDate || null,
        });
        toast.success("Item reported.");
        navigate(`/items/${data.id}`);
      }
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  if (isLoading) {
    return (
      <div className="page-loading">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="page">
      <h1 className="page-title">{isEditMode ? "Edit item" : "Report an item"}</h1>
      <p className="page-subtitle">
        {isEditMode ? "Update the details below." : "Fill in what you know - you can add photos after."}
      </p>

      <form className="card form-card" onSubmit={handleSubmit} noValidate>
        {!isEditMode && (
          <div className="form-field">
            <label className="form-label">
              Type<span className="required-mark">*</span>
            </label>
            <div className="radio-group">
              <label className="radio-option">
                <input
                  type="radio"
                  name="itemType"
                  value="LOST"
                  checked={form.itemType === "LOST"}
                  onChange={handleChange("itemType")}
                />
                Lost
              </label>
              <label className="radio-option">
                <input
                  type="radio"
                  name="itemType"
                  value="FOUND"
                  checked={form.itemType === "FOUND"}
                  onChange={handleChange("itemType")}
                />
                Found
              </label>
            </div>
            {errors.itemType && <span className="form-error-text">{errors.itemType}</span>}
          </div>
        )}

        <FormField
          label="Title"
          value={form.title}
          onChange={handleChange("title")}
          error={errors.title}
          required
        />

        <div className="form-field">
          <label className="form-label">Description</label>
          <textarea
            className="form-input"
            rows={4}
            value={form.description}
            onChange={handleChange("description")}
          />
        </div>

        <div className="form-field">
          <label className="form-label">
            Category<span className="required-mark">*</span>
          </label>
          <select
            className={`form-input ${errors.categoryId ? "form-input-error" : ""}`}
            value={form.categoryId}
            onChange={handleChange("categoryId")}
          >
            <option value="">Select a category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          {errors.categoryId && <span className="form-error-text">{errors.categoryId}</span>}
        </div>

        {isEditMode && (
          <div className="form-field">
            <label className="form-label">Status</label>
            <select className="form-input" value={form.status} onChange={handleChange("status")}>
              <option value="OPEN">Open</option>
              <option value="CLAIMED">Claimed</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>
        )}

        <FormField label="Location" value={form.location} onChange={handleChange("location")} />

        <div className="form-field">
          <label className="form-label">Date</label>
          <input
            type="date"
            className="form-input"
            value={form.eventDate}
            onChange={handleChange("eventDate")}
          />
        </div>

        <Button type="submit" loading={isSubmitting} fullWidth>
          {isEditMode ? "Save changes" : "Report item"}
        </Button>
      </form>
    </div>
  );
}
