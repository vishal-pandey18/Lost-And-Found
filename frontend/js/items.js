// Renders a single pinned "index card" for a lost/found item.
// `item` should be normalized to: { id, type, title, description, category,
// color, location, date, image, status, reporterName, userId }
// `actionsHtml` is optional extra markup (buttons) for the card footer.
function renderItemCard(item, actionsHtml = "", showContact = true) {
  const statusClass = `stamp-${item.status.toLowerCase()}`;
  const dateLabel = item.type === "LOST" ? "Lost" : "Found";
  const imageHtml = item.image
    ? `<img class="item-photo" src="${api.imageUrl(item.image)}" alt="${escapeHtml(item.title)}">`
    : "";

  const currentUser = getSession();
  const isOwnItem = currentUser && currentUser.id === item.userId;
  const contactBtn =
    showContact && !isOwnItem
      ? `<button class="btn btn-small contact-btn" data-user-id="${item.userId}" data-title="${escapeHtml(item.title)}">Contact Reporter</button>`
      : "";

  return `
    <div class="item-card">
      ${imageHtml}
      <span class="stamp ${statusClass}">${item.status}</span>
      <h3>${escapeHtml(item.title)}</h3>
      <div class="item-meta">${dateLabel} ${item.date || "—"} · ${escapeHtml(item.location || "Unknown location")}</div>
      <div class="item-meta">${escapeHtml(item.category || "Uncategorized")}${item.color ? " · " + escapeHtml(item.color) : ""}</div>
      <p class="item-desc">${escapeHtml(item.description || "No description provided.")}</p>
      <div class="item-card-footer">
        <span class="item-meta">Reported by ${escapeHtml(item.reporterName || "someone")}</span>
        ${contactBtn}
      </div>
      ${actionsHtml ? `<div class="item-actions mt-2">${actionsHtml}</div>` : ""}
    </div>
  `;
}

// Attach once per page that renders cards with a contact button.
// Uses event delegation so it works for dynamically-inserted cards.
function wireContactButtons(containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;

  container.addEventListener("click", async (e) => {
    const btn = e.target.closest(".contact-btn");
    if (!btn) return;

    const currentUser = getSession();
    if (!currentUser) {
      window.location.href = "login.html";
      return;
    }

    const receiverId = Number(btn.dataset.userId);
    const itemTitle = btn.dataset.title;
    const text = window.prompt(`Message to the reporter of "${itemTitle}":`);
    if (!text || !text.trim()) return;

    try {
      await api.sendMessage({ senderId: currentUser.id, receiverId, message: text.trim() });
      alert("Message sent!");
    } catch (err) {
      alert(`Couldn't send message: ${err.message}`);
    }
  });
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str ?? "";
  return div.innerHTML;
}

// Normalizes a LostItemResponse into the shared card shape.
function normalizeLost(item) {
  return {
    id: item.id,
    type: "LOST",
    title: item.title,
    description: item.description,
    category: item.category,
    color: item.color,
    location: item.location,
    date: item.lostDate,
    image: item.image,
    status: item.status,
    reporterName: item.reporterName,
    userId: item.userId,
  };
}

// Normalizes a FoundItemResponse into the shared card shape.
function normalizeFound(item) {
  return {
    id: item.id,
    type: "FOUND",
    title: item.title,
    description: item.description,
    category: item.category,
    color: item.color,
    location: item.location,
    date: item.foundDate,
    image: item.image,
    status: item.status,
    reporterName: item.reporterName,
    userId: item.userId,
  };
}
