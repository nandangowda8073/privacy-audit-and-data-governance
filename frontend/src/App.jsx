import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { DatabaseManagementPage } from './pages/DatabaseManagementPage';
import SchedulerPage from './pages/SchedulerPage';
import PiiDetectionPage from './pages/PiiDetectionPage';
import RiskClassificationPage from './pages/RiskClassificationPage';
import DataMaskingPage from './pages/DataMaskingPage';
import AuditLogsPage from './pages/AuditLogsPage';
import UserManagementPage from './pages/UserManagementPage';
import RolesPermissionsPage from './pages/RolesPermissionsPage';
import ReportsPage from './pages/ReportsPage';

function AppLayout() {
  const location = useLocation();
  const isDashboard = location.pathname.startsWith('/dashboard') || 
                      location.pathname.startsWith('/database') || 
                      location.pathname.startsWith('/scheduler') || 
                      location.pathname.startsWith('/pii') || 
                      location.pathname.startsWith('/risk') || 
                      location.pathname.startsWith('/masking') ||
                      location.pathname.startsWith('/audit') ||
                      location.pathname.startsWith('/reports') ||
                      location.pathname.startsWith('/users') ||
                      location.pathname.startsWith('/roles');

  return (
    <div className="min-h-screen bg-[#0a0d14] text-slate-100 flex flex-col selection:bg-cyan-500 selection:text-white">
      {!isDashboard && <Navbar />}
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/database" 
            element={
              <ProtectedRoute>
                <DatabaseManagementPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/databases" 
            element={
              <ProtectedRoute>
                <DatabaseManagementPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/scheduler" 
            element={
              <ProtectedRoute>
                <SchedulerPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/pii" 
            element={
              <ProtectedRoute>
                <PiiDetectionPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/risk" 
            element={
              <ProtectedRoute>
                <RiskClassificationPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/masking" 
            element={
              <ProtectedRoute>
                <DataMaskingPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/audit-logs" 
            element={
              <ProtectedRoute>
                <AuditLogsPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/users" 
            element={
              <ProtectedRoute>
                <UserManagementPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/roles" 
            element={
              <ProtectedRoute>
                <RolesPermissionsPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/reports" 
            element={
              <ProtectedRoute>
                <ReportsPage />
              </ProtectedRoute>
            } 
          />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppLayout />
      </AuthProvider>
    </Router>
  );
}

export default App;
