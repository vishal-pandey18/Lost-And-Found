export default function Button({
  children,
  variant = "primary",
  loading = false,
  disabled,
  type = "button",
  onClick,
  fullWidth,
}) {
  return (
    <button
      type={type}
      className={`btn btn-${variant} ${fullWidth ? "btn-full" : ""}`}
      disabled={disabled || loading}
      onClick={onClick}
    >
      {loading ? <span className="btn-spinner" aria-hidden="true" /> : null}
      {loading ? "Please wait..." : children}
    </button>
  );
}
