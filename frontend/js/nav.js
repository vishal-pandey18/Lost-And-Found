// Renders the corkboard nav bar into #nav-root on every page.
// `active` should match the current page's nav key for the highlight.
function renderNav(active) {
  const root = document.getElementById("nav-root");
  if (!root) return;

  const user = getSession();

  const links = [
    { key: "home", href: "index.html", label: "Home" },
    { key: "search", href: "search.html", label: "Search" },
  ];

  if (user) {
    links.push({ key: "dashboard", href: "dashboard.html", label: "Dashboard" });
    links.push({ key: "report-lost", href: "report-lost.html", label: "Report Lost" });
    links.push({ key: "report-found", href: "report-found.html", label: "Report Found" });
    links.push({ key: "profile", href: "profile.html", label: "Profile" });
    if (user.role === "ADMIN") {
      links.push({ key: "admin", href: "admin.html", label: "Admin" });
    }
  }

  const linkHtml = links
    .map(
      (l) =>
        `<a href="${l.href}" class="${l.key === active ? "active" : ""}">${l.label}</a>`
    )
    .join("");

  const authHtml = user
    ? `<button class="linklike" id="logout-btn">Logout (${user.name})</button>`
    : `<a href="login.html" class="${active === "login" ? "active" : ""}">Login</a>
       <a href="register.html" class="${active === "register" ? "active" : ""}">Register</a>`;

  root.innerHTML = `
    <div class="nav">
      <a href="index.html" class="brand">Lost<span>&amp;</span>Found</a>
      <div class="nav-links">
        ${linkHtml}
        ${authHtml}
      </div>
    </div>
  `;

  const logoutBtn = document.getElementById("logout-btn");
  if (logoutBtn) logoutBtn.addEventListener("click", logout);
}
