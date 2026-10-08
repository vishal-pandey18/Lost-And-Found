import { Link } from "react-router-dom";
import Badge from "./Badge";

export default function ItemCard({ item }) {
  const thumb = item.images?.[0]?.imageUrl;

  return (
    <Link to={`/items/${item.id}`} className="item-card">
      <div className="item-card-thumb">
        {thumb ? (
          <img src={thumb} alt={item.title} />
        ) : (
          <div className="item-card-thumb-placeholder">No photo</div>
        )}
      </div>
      <div className="item-card-body">
        <div className="item-card-badges">
          <Badge value={item.itemType} />
          <Badge value={item.status} />
        </div>
        <h3 className="item-card-title">{item.title}</h3>
        <p className="item-card-meta">
          {item.category?.name}
          {item.location ? ` · ${item.location}` : ""}
        </p>
      </div>
    </Link>
  );
}
