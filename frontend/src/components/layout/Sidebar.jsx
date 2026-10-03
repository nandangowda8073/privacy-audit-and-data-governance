import React from 'react';
import { 
  LayoutDashboard, Database, Search, History, 
  FileText, BarChart3, Users, Settings, ShieldAlert, ChevronRight, CalendarClock, 
  KeyRound, Eye, HelpCircle
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Sidebar = ({ activeTab, setActiveTab, isOpen }) => {
  const { hasPermission, hasAnyPermission } = useAuth();
  const allMenuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, badge: null, permission: 'DASHBOARD_VIEW' },
    { id: 'database', label: 'Database Management', icon: Database, badge: '14 Active', permission: 'DATABASE_VIEW' },
    { id: 'scan-center', label: 'Scan Center', icon: Search, badge: 'Ready', permission: 'SCAN_VIEW' },
    { id: 'scheduler', label: 'Scheduler', icon: CalendarClock, badge: null, permission: 'SCAN_SCHEDULE' },
    { id: 'pii-detection', label: 'PII Detection', icon: ShieldAlert, badge: null, permission: 'PII_VIEW' },
    { id: 'risk-classification', label: 'Risk Classification', icon: Eye, badge: null, permission: 'RISK_VIEW' },
    { id: 'data-masking', label: 'Data Masking', icon: ShieldAlert, badge: null, permission: 'MASKING_POLICY_VIEW' },
    { id: 'decisions', label: 'Explainable Decisions', icon: HelpCircle, badge: null, permission: 'DECISION_VIEW' },
    { id: 'scan-history', label: 'Scan History', icon: History, badge: null, permission: 'SCAN_VIEW' },
    { id: 'audit-logs', label: 'Audit Logs', icon: FileText, badge: null, permission: 'AUDIT_LOG_VIEW' },
    { id: 'reports', label: 'Reports', icon: BarChart3, badge: null, permission: 'REPORT_VIEW' },
    { id: 'user-management', label: 'User Management', icon: Users, badge: null, permission: 'USER_VIEW' },
    { id: 'roles-permissions', label: 'Roles & Permissions', icon: KeyRound, badge: null, permission: 'ROLE_VIEW' },
    { id: 'settings', label: 'Settings', icon: Settings, badge: null, permission: null },
  ];
  const menuItems = allMenuItems.filter(item => !item.permission || hasPermission(item.permission));

  return (
    <aside className={`
      fixed lg:static inset-y-0 left-0 z-30 w-64 glass-panel border-r border-slate-800 bg-[#0a0d14]/95 
      transform ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'} 
      transition-transform duration-200 ease-in-out flex flex-col justify-between p-4 overflow-y-auto
    `}>
      <div className="space-y-6">
        
        {/* Navigation Group Header */}
        <div>
          <div className="text-[10px] font-mono uppercase tracking-widest text-slate-500 font-semibold px-3 mb-3">
            Core Modules
          </div>
          <nav className="space-y-1.5">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`
                    w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-medium 
                    transition-all duration-150 group
                    ${isActive 
                      ? 'bg-gradient-to-r from-cyan-500/20 to-blue-500/10 border border-cyan-500/40 text-cyan-300 shadow-md shadow-cyan-500/10' 
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/60 border border-transparent'}
                  `}
                >
                  <div className="flex items-center gap-3">
                    <Icon className={`w-4 h-4 ${isActive ? 'text-cyan-400' : 'text-slate-500 group-hover:text-slate-300'}`} />
                    <span>{item.label}</span>
                  </div>

                  {item.badge && (
                    <span className={`
                      text-[10px] font-mono px-2 py-0.5 rounded-md border
                      ${isActive 
                        ? 'bg-cyan-500/20 border-cyan-500/40 text-cyan-300' 
                        : 'bg-slate-900 border-slate-800 text-slate-500'}
                    `}>
                      {item.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Security Alert Widget in Sidebar */}
        <div className="p-3.5 rounded-2xl bg-slate-950/80 border border-slate-800 space-y-2">
          <div className="flex items-center justify-between text-xs font-semibold text-rose-400">
            <span className="flex items-center gap-1.5 font-mono">
              <ShieldAlert className="w-4 h-4 text-rose-400" />
              Risk Posture
            </span>
            <span className="text-[10px] font-mono bg-rose-500/20 px-2 py-0.5 rounded text-rose-300">Moderate</span>
          </div>
          <p className="text-[11px] text-slate-400 leading-normal">
            12.45K High Risk PII records detected across 3 unencrypted database tables.
          </p>
          <button 
            onClick={() => setActiveTab('reports')}
            className="w-full text-center text-[11px] font-mono text-cyan-400 hover:text-cyan-300 flex items-center justify-center gap-1 pt-1"
          >
            View Threat Details <ChevronRight className="w-3 h-3" />
          </button>
        </div>

      </div>

      {/* Footer Version */}
      <div className="pt-4 border-t border-slate-800/80 text-[10px] font-mono text-slate-500 flex items-center justify-between">
        <span>v2.4.0-Enterprise</span>
        <span className="text-emerald-400 flex items-center gap-1">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-ping"></span> Live
        </span>
      </div>
    </aside>
  );
};
