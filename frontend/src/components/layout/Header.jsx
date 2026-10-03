import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { RoleBadge } from '../RoleBadge';
import { Shield, Search, Bell, LogOut, User, CheckCircle2, AlertTriangle, Menu, X } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Header = ({ onToggleSidebar, isSidebarOpen }) => {
  const { user, logout } = useAuth();
  const [showNotifications, setShowNotifications] = useState(false);

  const notifications = [
    { id: 1, title: 'High Risk PII Alert', desc: 'Unencrypted PAN numbers detected in Payment_Vault', time: '10m ago', type: 'warning' },
    { id: 2, title: 'Scan Completed', desc: 'Database Scan finished on Customer_DB', time: '25m ago', type: 'success' },
    { id: 3, title: 'New Auditor Joined', desc: 'Dr. Jane Miller assigned ROLE_AUDITOR', time: '1h ago', type: 'info' }
  ];

  return (
    <header className="glass-panel sticky top-0 z-40 border-b border-slate-800 bg-[#0a0d14]/90 backdrop-blur-md">
      <div className="px-4 sm:px-6 lg:px-8 py-3">
        <div className="flex items-center justify-between gap-4">
          
          {/* Left: Mobile Toggle & Brand Logo */}
          <div className="flex items-center gap-3">
            <button
              onClick={onToggleSidebar}
              className="lg:hidden p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-400 hover:text-white"
            >
              {isSidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>

            <Link to="/dashboard" className="flex items-center gap-3 group">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-cyan-500 to-blue-600 p-0.5 shadow-lg shadow-cyan-500/20 group-hover:shadow-cyan-500/40 transition-all">
                <div className="w-full h-full bg-[#0a0d14] rounded-[10px] flex items-center justify-center">
                  <Shield className="w-4 h-4 text-cyan-400 group-hover:scale-110 transition-transform" />
                </div>
              </div>
              <div>
                <span className="text-base font-bold bg-gradient-to-r from-white via-slate-200 to-cyan-400 bg-clip-text text-transparent hidden sm:inline-block">
                  Privacy Audit & PII Scanning Platform
                </span>
                <span className="text-sm font-bold text-white sm:hidden">PrivacyAudit.PII</span>
                <span className="block text-[9px] font-mono tracking-widest text-slate-500 uppercase">
                  Enterprise Security Monitoring
                </span>
              </div>
            </Link>
          </div>

          {/* Center: Search Bar */}
          <div className="hidden md:flex items-center flex-1 max-w-md mx-4">
            <div className="relative w-full">
              <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search databases, PII categories, audit logs, or users..."
                className="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-950/80 border border-slate-800 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500/80 transition-all font-mono"
              />
            </div>
          </div>

          {/* Right: Notifications, User Profile & Actions */}
          <div className="flex items-center gap-3">
            
            {/* Notification Bell */}
            <div className="relative">
              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative p-2 rounded-xl bg-slate-900/80 hover:bg-slate-800 border border-slate-800 text-slate-300 hover:text-white transition-all"
                title="Notifications"
              >
                <Bell className="w-4 h-4" />
                <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-cyan-400 animate-pulse"></span>
              </button>

              {showNotifications && (
                <div className="absolute right-0 mt-3 w-80 glass-panel rounded-2xl border border-slate-800 shadow-2xl p-4 space-y-3 z-50">
                  <div className="flex items-center justify-between pb-2 border-b border-slate-800">
                    <span className="text-xs font-bold text-white uppercase tracking-wider font-mono">
                      System Notifications
                    </span>
                    <span className="text-[10px] font-mono bg-cyan-500/20 text-cyan-300 px-2 py-0.5 rounded-full">
                      3 New
                    </span>
                  </div>
                  <div className="space-y-2.5 max-h-64 overflow-y-auto">
                    {notifications.map((n) => (
                      <div key={n.id} className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800/80 space-y-1">
                        <div className="flex items-center justify-between text-xs font-semibold text-white">
                          <span className="flex items-center gap-1.5">
                            {n.type === 'warning' && <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />}
                            {n.type === 'success' && <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />}
                            {n.title}
                          </span>
                          <span className="text-[10px] text-slate-500 font-mono">{n.time}</span>
                        </div>
                        <p className="text-[11px] text-slate-400">{n.desc}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* User Profile */}
            <div className="hidden sm:flex flex-col items-end">
              <span className="text-xs font-semibold text-slate-200">{user?.fullName}</span>
              <RoleBadge role={user?.role} />
            </div>

            <div className="h-6 w-px bg-slate-800 hidden sm:block"></div>

            <button
              onClick={logout}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 text-rose-400 hover:text-rose-300 text-xs font-semibold transition-all shadow-sm"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>

        </div>
      </div>
    </header>
  );
};
