export default function ItemFilterBar({ filters, categories, onChange }) {
  return (
    <div className="filter-bar">
      <select
        className="form-input filter-select"
        value={filters.itemType || ""}
        onChange={(e) => onChange({ ...filters, itemType: e.target.value || undefined })}
      >
        <option value="">All types</option>
        <option value="LOST">Lost</option>
        <option value="FOUND">Found</option>
      </select>

      <select
        className="form-input filter-select"
        value={filters.status || ""}
        onChange={(e) => onChange({ ...filters, status: e.target.value || undefined })}
      >
        <option value="">All statuses</option>
        <option value="OPEN">Open</option>
        <option value="CLAIMED">Claimed</option>
        <option value="RESOLVED">Resolved</option>
        <option value="CLOSED">Closed</option>
      </select>

      <select
        className="form-input filter-select"
        value={filters.categoryId || ""}
        onChange={(e) =>
          onChange({ ...filters, categoryId: e.target.value ? Number(e.target.value) : undefined })
        }
      >
        <option value="">All categories</option>
        {categories.map((c) => (
          <option key={c.id} value={c.id}>
            {c.name}
          </option>
        ))}
      </select>
    </div>
  );
}
