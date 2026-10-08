// Decodes a JWT's payload without verifying the signature - fine for reading
// display info client-side, since the backend independently verifies the
// token's validity on every request. Never trust this for authorization
// decisions; it's for showing "who am I" in the UI only.
export function decodeToken(token) {
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function isTokenExpired(token) {
  const decoded = decodeToken(token);
  if (!decoded?.exp) return true;
  return Date.now() >= decoded.exp * 1000;
}

// The backend's JWT now carries `sub` (email), `uid` (user id), and `role` -
// added specifically so the frontend can show/hide admin features without
// a separate "who am I" API call.
export function getUserFromToken(token) {
  const decoded = decodeToken(token);
  if (!decoded) return null;
  return {
    id: decoded.uid,
    email: decoded.sub,
    role: decoded.role,
  };
}
