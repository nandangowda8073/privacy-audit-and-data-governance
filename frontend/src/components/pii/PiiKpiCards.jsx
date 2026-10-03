import React from 'react';
import { ShieldAlert, AlertTriangle, Info, CheckCircle2, Activity, Clock } from 'lucide-react';

const statCards = [
  {
    key: 'totalDetected',
    label: 'Total PII Detected',
    icon: Activity,
    color: 'cyan',
    format: (v) => v?.toLocaleString() ?? '—',
  },
  {
    key: 'critical',
    label: 'Critical',
    icon: ShieldAlert,
    color: 'rose',
    format: (v) => v?.toLocaleString() ?? '—',
  },
  {
    key: 'high',
    label: 'High Risk',
    icon: AlertTriangle,
    color: 'amber',
    format: (v) => v?.toLocaleString() ?? '—',
  },
  {
    key: 'medium',
    label: 'Medium Risk',
    icon: Info,
    color: 'yellow',
    format: (v) => v?.toLocaleString() ?? '—',
  },
  {
    key: 'low',
    label: 'Low Risk',
    icon: CheckCircle2,
    color: 'emerald',
    format: (v) => v?.toLocaleString() ?? '—',
  },
  {
    key: 'avgConfidence',
    label: 'Avg Confidence',
    icon: Clock,
    color: 'purple',
    format: (v) => (v != null ? `${(v * 100).toFixed(1)}%` : '—'),
  },
];

const colorMap = {
  cyan:    { bg: 'bg-cyan-500/10',    border: 'border-cyan-500/20',    text: 'text-cyan-400'    },
  rose:    { bg: 'bg-rose-500/10',    border: 'border-rose-500/20',    text: 'text-rose-400'    },
  amber:   { bg: 'bg-amber-500/10',   border: 'border-amber-500/20',   text: 'text-amber-400'   },
  yellow:  { bg: 'bg-yellow-500/10',  border: 'border-yellow-500/20',  text: 'text-yellow-400'  },
  emerald: { bg: 'bg-emerald-500/10', border: 'border-emerald-500/20', text: 'text-emerald-400' },
  purple:  { bg: 'bg-purple-500/10',  border: 'border-purple-500/20',  text: 'text-purple-400'  },
};

const PiiKpiCards = ({ stats }) => (
  <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
    {statCards.map(({ key, label, icon: Icon, color, format }) => {
      const c = colorMap[color];
      return (
        <div key={key} className="glass-panel p-4 rounded-2xl border border-slate-800 flex flex-col gap-2">
          <div className={`w-9 h-9 rounded-xl flex items-center justify-center border ${c.bg} ${c.border}`}>
            <Icon className={`w-5 h-5 ${c.text}`} />
          </div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">{label}</p>
          <p className={`text-2xl font-black ${c.text}`}>{format(stats?.[key])}</p>
        </div>
      );
    })}
  </div>
);

export default PiiKpiCards;
