// tone maps a semantic value to a visual style. Kept generic so it works
// for both item_type (LOST/FOUND) and status (OPEN/CLAIMED/RESOLVED/CLOSED).
const TONE_MAP = {
  LOST: "badge-warning",
  FOUND: "badge-info",
  OPEN: "badge-success",
  CLAIMED: "badge-warning",
  RESOLVED: "badge-muted",
  CLOSED: "badge-muted",
  PENDING: "badge-warning",
  APPROVED: "badge-success",
  REJECTED: "badge-error",
};

export default function Badge({ value }) {
  const tone = TONE_MAP[value] || "badge-muted";
  return <span className={`badge ${tone}`}>{value}</span>;
}
