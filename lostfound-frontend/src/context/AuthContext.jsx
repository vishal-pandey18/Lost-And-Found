import { createContext, useContext, useEffect, useState, useCallback } from "react";
import * as authService from "../services/authService";
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from "../services/api";
import { getUserFromToken, isTokenExpired } from "../utils/jwt";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getAccessToken();
    if (token && !isTokenExpired(token)) {
      setUser(getUserFromToken(token));
    } else if (getRefreshToken()) {
      // Access token expired but we have a refresh token - the axios
      // interceptor will handle refreshing on the first real request.
      // For the initial load, optimistically trust the refresh token exists
      // and let the first API call sort out validity.
      setUser(getUserFromToken(token) || { id: null, email: null });
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(async (credentials) => {
    const { data } = await authService.login(credentials);
    setTokens(data);
    setUser(getUserFromToken(data.accessToken));
    return data;
  }, []);

  const register = useCallback(async (details) => {
    const { data } = await authService.register(details);
    setTokens(data);
    setUser(getUserFromToken(data.accessToken));
    return data;
  }, []);

  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
  }, []);

  const value = {
    user,
    isAuthenticated: !!user,
    isAdmin: user?.role === "ADMIN",
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
