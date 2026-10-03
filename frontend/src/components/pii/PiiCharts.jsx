import React from 'react';
import {
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Legend
} from 'recharts';
import { BarChart3 } from 'lucide-react';

const CATEGORY_COLORS = {
  PERSONAL_IDENTITY:   '#f43f5e',
  FINANCIAL:           '#f59e0b',
  CONTACT:             '#06b6d4',
  PERSONAL_INFORMATION:'#8b5cf6',
  CUSTOM:              '#10b981',
};

const RISK_COLORS = {
  CRITICAL: '#ef4444',
  HIGH:     '#f97316',
  MEDIUM:   '#eab308',
  LOW:      '#22c55e',
};

const METHOD_COLORS = {
  REGEX:           '#06b6d4',
  CUSTOM_PATTERN:  '#8b5cf6',
  PRESIDIO:        '#f59e0b',
};

const friendlyLabel = (key) =>
  key.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());

const CustomTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs font-mono">
      <p className="text-white font-bold">{friendlyLabel(payload[0].name)}</p>
      <p className="text-cyan-400">{payload[0].value} entities</p>
    </div>
  );
};

const PiiCharts = ({ history }) => {
  // Aggregate from history array
  const byCategory = {};
  const byRisk     = {};
  const byMethod   = {};

  (history || []).forEach(({ category, riskLevel, detectionMethod }) => {
    if (category)        byCategory[category]        = (byCategory[category] || 0) + 1;
    if (riskLevel)       byRisk[riskLevel]            = (byRisk[riskLevel] || 0) + 1;
    if (detectionMethod) byMethod[detectionMethod]    = (byMethod[detectionMethod] || 0) + 1;
  });

  const categoryData = Object.entries(byCategory).map(([name, value]) => ({ name, value }));
  const riskData     = Object.entries(byRisk).map(([name, value]) => ({ name, value }));
  const methodData   = Object.entries(byMethod).map(([name, value]) => ({ name, value }));

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

      {/* Category Pie */}
      <div className="glass-panel p-5 rounded-2xl border border-slate-800">
        <h3 className="text-sm font-semibold text-white mb-4 flex items-center gap-2">
          <BarChart3 className="w-4 h-4 text-cyan-400" /> PII by Category
        </h3>
        {categoryData.length === 0 ? (
          <p className="text-slate-500 text-xs text-center py-8">No data yet</p>
        ) : (
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={categoryData} dataKey="value" nameKey="name" outerRadius={75} paddingAngle={3}>
                {categoryData.map(({ name }) => (
                  <Cell key={name} fill={CATEGORY_COLORS[name] || '#64748b'} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>
        )}
        <div className="mt-3 flex flex-wrap gap-2">
          {categoryData.map(({ name }) => (
            <span key={name} className="flex items-center gap-1 text-[10px] font-mono text-slate-400">
              <span className="w-2 h-2 rounded-full inline-block" style={{ background: CATEGORY_COLORS[name] || '#64748b' }} />
              {friendlyLabel(name)}
            </span>
          ))}
        </div>
      </div>

      {/* Risk Bar */}
      <div className="glass-panel p-5 rounded-2xl border border-slate-800">
        <h3 className="text-sm font-semibold text-white mb-4 flex items-center gap-2">
          <BarChart3 className="w-4 h-4 text-rose-400" /> Risk Distribution
        </h3>
        {riskData.length === 0 ? (
          <p className="text-slate-500 text-xs text-center py-8">No data yet</p>
        ) : (
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={riskData} margin={{ top: 5, right: 5, bottom: 5, left: -20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
              <XAxis dataKey="name" tick={{ fontSize: 9, fill: '#94a3b8' }} />
              <YAxis tick={{ fontSize: 9, fill: '#94a3b8' }} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                {riskData.map(({ name }) => (
                  <Cell key={name} fill={RISK_COLORS[name] || '#64748b'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Detection Method Pie */}
      <div className="glass-panel p-5 rounded-2xl border border-slate-800">
        <h3 className="text-sm font-semibold text-white mb-4 flex items-center gap-2">
          <BarChart3 className="w-4 h-4 text-purple-400" /> Detection Method
        </h3>
        {methodData.length === 0 ? (
          <p className="text-slate-500 text-xs text-center py-8">No data yet</p>
        ) : (
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={methodData} dataKey="value" nameKey="name" outerRadius={75} paddingAngle={3}>
                {methodData.map(({ name }) => (
                  <Cell key={name} fill={METHOD_COLORS[name] || '#64748b'} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>
        )}
        <div className="mt-3 flex flex-wrap gap-2">
          {methodData.map(({ name }) => (
            <span key={name} className="flex items-center gap-1 text-[10px] font-mono text-slate-400">
              <span className="w-2 h-2 rounded-full inline-block" style={{ background: METHOD_COLORS[name] || '#64748b' }} />
              {friendlyLabel(name)}
            </span>
          ))}
        </div>
      </div>

    </div>
  );
};

export default PiiCharts;
