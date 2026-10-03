import React from 'react';
import { ShieldCheck, Activity, Database, Cpu, Lock, Clock, Calendar, ShieldAlert, KeyRound } from 'lucide-react';

export const StatusWidgetBar = ({ overview }) => {
  const complianceScore = overview?.complianceScore !== undefined ? overview.complianceScore : (overview?.privacyRiskScore || 100);
  const accessSecurity = overview?.accessSecurityMetrics || { totalAccessAttempts: 0, accessDeniedCount: 0, accessDeniedRate: 0.0 };

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-emerald-400 border-emerald-500/30 bg-emerald-500/10';
    if (score >= 60) return 'text-amber-400 border-amber-500/30 bg-amber-500/10';
    return 'text-rose-400 border-rose-500/30 bg-rose-500/10';
  };

  return (
    <div className="glass-panel p-6 rounded-3xl border border-slate-800 bg-gradient-to-r from-[#0a0d14] via-[#111625] to-[#0f172a] space-y-6 shadow-xl">
      
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
        
        {/* Compliance Score Gauge & Explanation */}
        <div className="md:col-span-6 flex items-center gap-6">
          <div className="relative flex-shrink-0">
            <div className={`w-20 h-20 rounded-full border-4 flex flex-col items-center justify-center ${getScoreColor(complianceScore)} shadow-lg`}>
              <span className="text-xl font-extrabold font-mono tracking-tighter">{complianceScore.toFixed(1)}%</span>
              <span className="text-[9px] uppercase font-mono font-bold tracking-widest text-slate-400">Score</span>
            </div>
          </div>

          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono uppercase tracking-widest text-slate-400 font-semibold">
                Mathematical Compliance Score
              </span>
              <span className={`px-2 py-0.5 rounded text-[10px] font-mono border ${
                complianceScore >= 80 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' :
                complianceScore >= 60 ? 'bg-amber-500/20 text-amber-300 border-amber-500/30' :
                'bg-rose-500/20 text-rose-300 border-rose-500/30'
              }`}>
                {complianceScore >= 80 ? 'Healthy' : complianceScore >= 60 ? 'Moderate' : 'Critical'}
              </span>
            </div>
            <h3 className="text-base font-bold text-white">Privacy & Risk Compliance Posture</h3>
            <p className="text-xs text-slate-400 leading-relaxed font-mono">
              Formula: 100 - (Crit×5 + High×2 + Med×0.5 + Low×0.1 + Violations×10)
            </p>
          </div>
        </div>

        {/* Separate Access Security Metrics Widget */}
        <div className="md:col-span-6 p-4 rounded-2xl bg-slate-950/80 border border-slate-800 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-xs font-bold text-slate-200">
              <KeyRound className="w-4 h-4 text-purple-400" />
              <span>Access Security Metrics (RBAC)</span>
            </div>
            <span className="text-[10px] font-mono text-slate-400">Tracked Separately</span>
          </div>

          <div className="grid grid-cols-3 gap-3 font-mono text-xs">
            <div className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-0.5">
              <div className="text-[10px] text-slate-400">Total Attempts</div>
              <div className="text-sm font-bold text-cyan-400">{accessSecurity.totalAccessAttempts}</div>
            </div>

            <div className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-0.5">
              <div className="text-[10px] text-slate-400">Access Denied</div>
              <div className="text-sm font-bold text-rose-400">{accessSecurity.accessDeniedCount}</div>
            </div>

            <div className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-0.5">
              <div className="text-[10px] text-slate-400">Denied Rate</div>
              <div className="text-sm font-bold text-amber-400">{accessSecurity.accessDeniedRate.toFixed(1)}%</div>
            </div>
          </div>
        </div>

      </div>

    </div>
  );
};
