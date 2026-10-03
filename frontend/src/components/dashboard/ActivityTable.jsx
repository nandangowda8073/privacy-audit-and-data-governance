import React from 'react';
import { Clock, CheckCircle2, AlertTriangle, RefreshCw, UserCheck } from 'lucide-react';

export const ActivityTable = ({ activities }) => {
  const defaultActivities = [
    { id: '1', time: '09:45 AM', user: 'Super Admin', userRole: 'ROLE_SUPER_ADMIN', action: 'Added Employee Database', status: 'COMPLETED' },
    { id: '2', time: '09:20 AM', user: 'Security Officer', userRole: 'ROLE_SECURITY_OFFICER', action: 'Database Scan Completed', status: 'COMPLETED' },
    { id: '3', time: '08:15 AM', user: 'Compliance Auditor', userRole: 'ROLE_AUDITOR', action: 'Generated GDPR Compliance Report', status: 'SUCCESS' },
    { id: '4', time: '07:30 AM', user: 'Security Officer', userRole: 'ROLE_SECURITY_OFFICER', action: 'High Risk PII Alert Triggered', status: 'WARNING' },
  ];

  const list = activities && activities.length > 0 ? activities : defaultActivities;

  const getStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
      case 'SUCCESS':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono font-semibold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            <CheckCircle2 className="w-3 h-3" /> Completed
          </span>
        );
      case 'WARNING':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono font-semibold bg-amber-500/10 border border-amber-500/30 text-amber-400">
            <AlertTriangle className="w-3 h-3" /> Warning
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono font-semibold bg-cyan-500/10 border border-cyan-500/30 text-cyan-400">
            <RefreshCw className="w-3 h-3 animate-spin" /> In Progress
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono font-semibold bg-slate-500/10 border border-slate-500/30 text-slate-400">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Clock className="w-4 h-4 text-cyan-400" />
          <h3 className="text-sm font-bold text-white uppercase tracking-wider">Recent System Activities</h3>
        </div>
        <span className="text-[10px] font-mono text-slate-500">Live Event Feed</span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs font-mono">
          <thead>
            <tr className="border-b border-slate-800 text-slate-400 text-[11px] uppercase tracking-wider">
              <th className="py-3 px-3">Time</th>
              <th className="py-3 px-3">User & Role</th>
              <th className="py-3 px-3">Action Description</th>
              <th className="py-3 px-3 text-right">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {list.map((act) => (
              <tr key={act.id} className="hover:bg-slate-900/40 transition-colors">
                <td className="py-3.5 px-3 text-cyan-400 font-semibold whitespace-nowrap">{act.time}</td>
                <td className="py-3.5 px-3 whitespace-nowrap">
                  <div className="flex items-center gap-2">
                    <UserCheck className="w-3.5 h-3.5 text-slate-400" />
                    <div>
                      <span className="text-white font-medium block">{act.user}</span>
                      <span className="text-[10px] text-slate-500">{act.userRole}</span>
                    </div>
                  </div>
                </td>
                <td className="py-3.5 px-3 text-slate-300">{act.action}</td>
                <td className="py-3.5 px-3 text-right whitespace-nowrap">{getStatusBadge(act.status)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
