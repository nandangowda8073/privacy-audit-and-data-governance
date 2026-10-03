import React from 'react';
import { 
  Database, Table, FileSpreadsheet, Search, 
  ShieldAlert, AlertTriangle, FileX, Users, TrendingUp, TrendingDown 
} from 'lucide-react';

export const KpiCard = ({ title, value, change, isPositive, iconType, subtitle, colorScheme }) => {
  const getIcon = () => {
    switch (iconType) {
      case 'databases': return <Database className="w-5 h-5 text-cyan-400" />;
      case 'tables': return <Table className="w-5 h-5 text-indigo-400" />;
      case 'records': return <FileSpreadsheet className="w-5 h-5 text-emerald-400" />;
      case 'scanned': return <Search className="w-5 h-5 text-teal-400" />;
      case 'pii': return <ShieldAlert className="w-5 h-5 text-amber-400" />;
      case 'highRisk': return <AlertTriangle className="w-5 h-5 text-rose-400" />;
      case 'violations': return <FileX className="w-5 h-5 text-purple-400" />;
      case 'users': return <Users className="w-5 h-5 text-blue-400" />;
      default: return <Database className="w-5 h-5 text-cyan-400" />;
    }
  };

  const getColorBorder = () => {
    switch (colorScheme) {
      case 'cyan': return 'border-cyan-500/20 bg-gradient-to-br from-cyan-500/10 via-slate-900/40 to-slate-950/80';
      case 'indigo': return 'border-indigo-500/20 bg-gradient-to-br from-indigo-500/10 via-slate-900/40 to-slate-950/80';
      case 'emerald': return 'border-emerald-500/20 bg-gradient-to-br from-emerald-500/10 via-slate-900/40 to-slate-950/80';
      case 'amber': return 'border-amber-500/20 bg-gradient-to-br from-amber-500/10 via-slate-900/40 to-slate-950/80';
      case 'rose': return 'border-rose-500/20 bg-gradient-to-br from-rose-500/10 via-slate-900/40 to-slate-950/80';
      case 'purple': return 'border-purple-500/20 bg-gradient-to-br from-purple-500/10 via-slate-900/40 to-slate-950/80';
      default: return 'border-slate-800 bg-slate-950/60';
    }
  };

  return (
    <div className={`glass-panel p-5 rounded-2xl border ${getColorBorder()} transition-all duration-200 hover:scale-[1.02] shadow-lg`}>
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{title}</span>
        <div className="p-2 rounded-xl bg-slate-900/80 border border-slate-800">
          {getIcon()}
        </div>
      </div>

      <div className="mt-3 flex items-baseline justify-between">
        <span className="text-2xl font-extrabold text-white tracking-tight font-mono">{value}</span>
        
        {change && (
          <span className={`inline-flex items-center gap-1 text-[11px] font-mono font-medium px-2 py-0.5 rounded-md ${
            isPositive ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
          }`}>
            {isPositive ? <TrendingUp className="w-3 h-3" /> : <TrendingDown className="w-3 h-3" />}
            {change}
          </span>
        )}
      </div>

      {subtitle && (
        <p className="mt-1 text-[11px] text-slate-500 font-mono">{subtitle}</p>
      )}
    </div>
  );
};
