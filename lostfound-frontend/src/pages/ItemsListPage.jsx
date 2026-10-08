import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { searchItems } from "../services/itemService";
import { getCategories } from "../services/categoryService";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { getErrorMessage } from "../utils/errorMessage";
import ItemCard from "../components/ItemCard";
import ItemFilterBar from "../components/ItemFilterBar";
import Pagination from "../components/Pagination";
import Spinner from "../components/Spinner";
import Button from "../components/Button";

export default function ItemsListPage() {
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({});
  const [page, setPageState] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [searchText, setSearchText] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  useDocumentTitle("Browse Items");

  useEffect(() => {
    getCategories()
      .then(({ data }) => setCategories(data))
      .catch(() => {
        /* category list is a filter convenience - fail silently, filters still work without it */
      });
  }, []);

  const loadItems = useCallback(() => {
    setIsLoading(true);
    setLoadError(null);
    searchItems({ ...filters, page: pageNumber, size: 12 })
      .then(({ data }) => setPageState(data))
      .catch((err) => setLoadError(getErrorMessage(err)))
      .finally(() => setIsLoading(false));
  }, [filters, pageNumber]);

  useEffect(() => {
    loadItems();
  }, [loadItems]);

  function handleFilterChange(next) {
    setFilters(next);
    setPageNumber(0);
  }

  // The backend doesn't expose full-text search - this filters the current
  // page's already-loaded results client-side, rather than faking a
  // server-side search that doesn't exist.
  const visibleItems = (page?.content || []).filter((item) => {
    if (!searchText.trim()) return true;
    const haystack = `${item.title} ${item.description || ""}`.toLowerCase();
    return haystack.includes(searchText.toLowerCase());
  });

  return (
    <div className="page">
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Browse Items</h1>
          <p className="page-subtitle">Lost and found reports from the community.</p>
        </div>
        <Link to="/items/new">
          <Button variant="primary">Report an item</Button>
        </Link>
      </div>

      <div className="list-controls">
        <input
          type="text"
          className="form-input search-input"
          placeholder="Search this page's results..."
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
        />
        <ItemFilterBar filters={filters} categories={categories} onChange={handleFilterChange} />
      </div>

      {isLoading && (
        <div className="page-loading">
          <Spinner />
        </div>
      )}

      {!isLoading && loadError && (
        <div className="empty-state">
          <p className="empty-state-title">Couldn't load items</p>
          <p className="empty-state-text">{loadError}</p>
          <Button variant="secondary" onClick={loadItems}>
            Retry
          </Button>
        </div>
      )}

      {!isLoading && !loadError && visibleItems.length === 0 && (
        <div className="empty-state">
          <p className="empty-state-title">No items found</p>
          <p className="empty-state-text">Try adjusting your filters or search.</p>
        </div>
      )}

      {!isLoading && !loadError && visibleItems.length > 0 && (
        <>
          <div className="item-grid">
            {visibleItems.map((item) => (
              <ItemCard key={item.id} item={item} />
            ))}
          </div>
          <Pagination page={page} onPageChange={setPageNumber} />
        </>
      )}
    </div>
  );
}
