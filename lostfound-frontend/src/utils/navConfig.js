// Single source of truth for navigation links, so the sidebar and mobile
// drawer never drift out of sync. Only routes that actually exist should be
// listed here - add an entry in the same phase you build its page.
export const NAV_ITEMS = [
  { label: "Dashboard", path: "/", icon: "home" },
  { label: "Browse Items", path: "/items", icon: "search" },
  { label: "Report Item", path: "/items/new", icon: "plus" },
  { label: "My Claims", path: "/claims/mine", icon: "edit" },
  { label: "Notifications", path: "/notifications", icon: "bell" },
  { label: "Alert Subscriptions", path: "/subscriptions", icon: "search" },
  { label: "Claim Queue", path: "/admin/claims", icon: "bell", adminOnly: true },
];
