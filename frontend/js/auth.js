// Lightweight session handling. There's no JWT yet (see backend README),
// so we just remember who's logged in locally and send their id with
// requests that need to know who's acting.

const SESSION_KEY = "lf_current_user";

function saveSession(user) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(user));
}

function getSession() {
  const raw = localStorage.getItem(SESSION_KEY);
  return raw ? JSON.parse(raw) : null;
}

function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}

// Call at the top of any page that requires login.
function requireAuth() {
  const user = getSession();
  if (!user) {
    window.location.href = "login.html";
    return null;
  }
  return user;
}

function logout() {
  clearSession();
  window.location.href = "login.html";
}
