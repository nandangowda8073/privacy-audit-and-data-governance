import React from 'react';
import { ShieldAlert, Shield, ShieldCheck, ShieldBan } from 'lucide-react';

const RiskKpiCards = ({ summary }) => {
  const cards = [
    {
      key: 'critical',
      label: 'Critical Risk',
      value: summary?.critical || 0,
      icon: ShieldAlert,
      color: 'rose',
      bgClass: 'bg-rose-500/10',
      textClass: 'text-rose-400',
      borderClass: 'border-rose-500/20'
    },
    {
      key: 'high',
      label: 'High Risk',
      value: summary?.high || 0,
      icon: ShieldBan,
      color: 'amber',
      bgClass: 'bg-amber-500/10',
      textClass: 'text-amber-400',
      borderClass: 'border-amber-500/20'
    },
    {
      key: 'medium',
      label: 'Medium Risk',
      value: summary?.medium || 0,
      icon: Shield,
      color: 'yellow',
      bgClass: 'bg-yellow-500/10',
      textClass: 'text-yellow-400',
      borderClass: 'border-yellow-500/20'
    },
    {
      key: 'low',
      label: 'Low Risk',
      value: summary?.low || 0,
      icon: ShieldCheck,
      color: 'emerald',
      bgClass: 'bg-emerald-500/10',
      textClass: 'text-emerald-400',
      borderClass: 'border-emerald-500/20'
    }
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <div key={card.key} className={`glass-panel p-5 rounded-2xl border ${card.borderClass} ${card.bgClass} flex items-center justify-between`}>
            <div>
              <p className={`text-xs font-semibold uppercase tracking-wider ${card.textClass}`}>
                {card.label}
              </p>
              <h3 className="text-3xl font-bold text-white mt-1">
                {card.value.toLocaleString()}
              </h3>
            </div>
            <div className={`p-3 rounded-xl bg-slate-950/50`}>
              <Icon className={`w-7 h-7 ${card.textClass}`} />
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default RiskKpiCards;
