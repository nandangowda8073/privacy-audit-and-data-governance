import React, { createContext, useContext, useState, useEffect } from 'react';
import API from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verifyToken = async () => {
      if (token) {
        try {
          const response = await API.get('/auth/me');
          if (response.data && response.data.success) {
            setUser(response.data.data);
            localStorage.setItem('user', JSON.stringify(response.data.data));
          }
        } catch (error) {
          console.error("Session verification failed:", error);
          logout();
        }
      }
      setLoading(false);
    };

    verifyToken();
  }, [token]);

  const login = async (usernameOrEmail, password) => {
    try {
      const response = await API.post('/auth/login', { usernameOrEmail, password });
      if (response.data && response.data.success) {
        const { token: jwtToken, user: userData } = response.data.data;
        setToken(jwtToken);
        setUser(userData);
        localStorage.setItem('token', jwtToken);
        localStorage.setItem('user', JSON.stringify(userData));
        return { success: true, user: userData };
      } else {
        return { success: false, message: response.data.message || 'Login failed' };
      }
    } catch (error) {
      const msg = error.response?.data?.message || 'Invalid credentials or server unavailable';
      return { success: false, message: msg };
    }
  };

  const register = async (formData) => {
    try {
      const response = await API.post('/auth/register', formData);
      if (response.data && response.data.success) {
        return { success: true, message: 'Registration successful! You can now log in.' };
      } else {
        return { success: false, message: response.data.message || 'Registration failed' };
      }
    } catch (error) {
      const msg = error.response?.data?.message || 'Registration failed. Check your inputs.';
      return { success: false, message: msg };
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const hasRole = (requiredRoles) => {
    if (!user) return false;
    if (Array.isArray(requiredRoles)) {
      return requiredRoles.includes(user.role);
    }
    return user.role === requiredRoles;
  };

  const hasPermission = (permission) => {
    if (!user || !user.permissions) return false;
    return user.permissions.includes(permission);
  };

  const hasAnyPermission = (permissions = []) => {
    if (!user || !user.permissions) return false;
    return permissions.some(p => user.permissions.includes(p));
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, hasRole, hasPermission, hasAnyPermission, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
