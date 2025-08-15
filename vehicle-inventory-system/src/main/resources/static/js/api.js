// Tiny fetch wrapper: attaches the JWT, centralizes error handling, and bounces
// to the login page on a 401 so no page has to duplicate that logic.
const Api = (() => {
  const TOKEN_KEY = "vims_token";
  const USERNAME_KEY = "vims_username";

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function setSession(token, username) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USERNAME_KEY, username);
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
  }

  function getUsername() {
    return localStorage.getItem(USERNAME_KEY);
  }

  function requireAuth() {
    if (!getToken()) {
      window.location.href = "/login.html";
    }
  }

  async function request(path, options = {}) {
    const headers = options.headers ? { ...options.headers } : {};
    const token = getToken();
    if (token) {
      headers["Authorization"] = "Bearer " + token;
    }
    if (options.body && !(options.body instanceof FormData) && !headers["Content-Type"]) {
      headers["Content-Type"] = "application/json";
    }

    const response = await fetch(path, { ...options, headers });

    if (response.status === 401) {
      clearSession();
      window.location.href = "/login.html";
      throw new Error("Not authenticated");
    }

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json") ? await response.json() : await response.text();

    if (!response.ok) {
      const message = (payload && payload.message) || `Request failed (${response.status})`;
      const error = new Error(message);
      error.payload = payload;
      throw error;
    }

    return payload;
  }

  return {
    get: (path) => request(path, { method: "GET" }),
    post: (path, body) => request(path, { method: "POST", body: body instanceof FormData ? body : JSON.stringify(body) }),
    put: (path, body) => request(path, { method: "PUT", body: body instanceof FormData ? body : JSON.stringify(body) }),
    del: (path) => request(path, { method: "DELETE" }),
    login: (username, password) =>
      request("/api/auth/login", { method: "POST", body: JSON.stringify({ username, password }) }),
    setSession,
    clearSession,
    getUsername,
    requireAuth,
  };
})();
