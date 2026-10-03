import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, isAuthenticated, loading, hasRole } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div class="min-h-screen bg-[#0a0d14] flex items-center justify-center">
        <div class="flex flex-col items-center space-y-4">
          <div class="w-12 h-12 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin"></div>
          <p class="text-slate-400 text-sm font-mono animate-pulse">Verifying Security Credentials...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !hasRole(allowedRoles)) {
    return (
      <div class="min-h-screen bg-[#0a0d14] flex items-center justify-center p-6">
        <div class="glass-panel max-w-md w-full p-8 rounded-2xl border border-rose-500/30 text-center space-y-4">
          <div class="w-16 h-16 bg-rose-500/10 rounded-full flex items-center justify-center mx-auto border border-rose-500/30">
            <span class="text-2xl text-rose-500 font-bold">403</span>
          </div>
          <h2 class="text-xl font-semibold text-white">Access Restricted</h2>
          <p class="text-slate-400 text-sm">
            Your current role <span class="font-mono text-cyan-400">({user?.role})</span> does not have authorization to view this resource.
          </p>
          <a href="/dashboard" class="inline-block px-5 py-2.5 bg-cyan-500/20 hover:bg-cyan-500/30 border border-cyan-500/40 text-cyan-300 rounded-xl text-sm font-medium transition-all">
            Return to Safety
          </a>
        </div>
      </div>
    );
  }

  return children;
};
