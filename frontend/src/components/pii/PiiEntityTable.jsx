import React, { useState } from 'react';
import { List, Search, X } from 'lucide-react';

const RISK_STYLES = {
  CRITICAL: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
  HIGH:     'bg-amber-500/10 text-amber-400 border-amber-500/20',
  MEDIUM:   'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
  LOW:      'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
};

const friendlyLabel = (key) =>
  (key || '').replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());

const PiiEntityTable = ({ entities }) => {
  const [search, setSearch] = useState('');
  const [filterRisk, setFilterRisk] = useState('ALL');
  const [filterCategory, setFilterCategory] = useState('ALL');
  const [filterMethod, setFilterMethod] = useState('ALL');

  const filtered = (entities || []).filter((e) => {
    const q = search.toLowerCase();
    const matchSearch =
      !q ||
      e.entityType?.toLowerCase().includes(q) ||
      e.maskedValue?.toLowerCase().includes(q) ||
      e.source?.toLowerCase().includes(q);
    const matchRisk = filterRisk === 'ALL' || e.riskLevel === filterRisk;
    const matchCat  = filterCategory === 'ALL' || e.category === filterCategory;
    const matchMeth = filterMethod === 'ALL' || e.detectionMethod === filterMethod;
    return matchSearch && matchRisk && matchCat && matchMeth;
  });

  return (
    <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden">
      {/* Toolbar */}
      <div className="p-4 border-b border-slate-800 flex flex-wrap gap-3 items-center">
        <h3 className="text-sm font-semibold text-white flex items-center gap-2 mr-2">
          <List className="w-4 h-4 text-cyan-400" /> Detected Entities
        </h3>

        {/* Search */}
        <div className="relative flex-1 min-w-[180px]">
          <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search entity, value, source..."
            className="w-full pl-8 pr-8 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:border-cyan-500 outline-none"
          />
          {search && (
            <button onClick={() => setSearch('')} className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white">
              <X className="w-3 h-3" />
            </button>
          )}
        </div>

        {/* Risk filter */}
        <select
          value={filterRisk}
          onChange={(e) => setFilterRisk(e.target.value)}
          className="text-xs bg-slate-900 border border-slate-700 text-slate-300 rounded-xl px-3 py-2 focus:border-cyan-500 outline-none"
        >
          {['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map((r) => (
            <option key={r} value={r}>{r === 'ALL' ? 'All Risks' : r}</option>
          ))}
        </select>

        {/* Category filter */}
        <select
          value={filterCategory}
          onChange={(e) => setFilterCategory(e.target.value)}
          className="text-xs bg-slate-900 border border-slate-700 text-slate-300 rounded-xl px-3 py-2 focus:border-cyan-500 outline-none"
        >
          {['ALL', 'PERSONAL_IDENTITY', 'FINANCIAL', 'CONTACT', 'PERSONAL_INFORMATION', 'CUSTOM'].map((c) => (
            <option key={c} value={c}>{c === 'ALL' ? 'All Categories' : friendlyLabel(c)}</option>
          ))}
        </select>

        {/* Method filter */}
        <select
          value={filterMethod}
          onChange={(e) => setFilterMethod(e.target.value)}
          className="text-xs bg-slate-900 border border-slate-700 text-slate-300 rounded-xl px-3 py-2 focus:border-cyan-500 outline-none"
        >
          {['ALL', 'REGEX', 'CUSTOM_PATTERN', 'PRESIDIO'].map((m) => (
            <option key={m} value={m}>{m === 'ALL' ? 'All Methods' : friendlyLabel(m)}</option>
          ))}
        </select>

        <span className="ml-auto text-[11px] font-mono text-slate-500">
          {filtered.length} results
        </span>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-slate-800 bg-slate-950/60 text-slate-400 font-mono text-[11px] uppercase">
              <th className="py-3 px-4">Entity</th>
              <th className="py-3 px-4">Category</th>
              <th className="py-3 px-4">Masked Value</th>
              <th className="py-3 px-4">Risk</th>
              <th className="py-3 px-4">Confidence</th>
              <th className="py-3 px-4">Method</th>
              <th className="py-3 px-4">Source</th>
              <th className="py-3 px-4">Detected At</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/50">
            {filtered.length === 0 ? (
              <tr>
                <td colSpan="8" className="py-12 text-center text-slate-500 text-xs">
                  No PII entities found. Run a detection scan to see results here.
                </td>
              </tr>
            ) : (
              filtered.map((e) => (
                <tr key={e.id} className="hover:bg-slate-800/20 transition-colors">
                  <td className="py-3 px-4 font-mono font-semibold text-slate-200">
                    {friendlyLabel(e.entityType)}
                  </td>
                  <td className="py-3 px-4 text-slate-400 font-mono">
                    {friendlyLabel(e.category)}
                  </td>
                  <td className="py-3 px-4 font-mono text-amber-300">
                    {e.maskedValue}
                  </td>
                  <td className="py-3 px-4">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold border ${RISK_STYLES[e.riskLevel] || 'text-slate-400'}`}>
                      {e.riskLevel}
                    </span>
                  </td>
                  <td className="py-3 px-4 font-mono text-cyan-400">
                    {e.confidenceScore != null ? `${(e.confidenceScore * 100).toFixed(0)}%` : '—'}
                  </td>
                  <td className="py-3 px-4 font-mono text-purple-400">
                    {friendlyLabel(e.detectionMethod)}
                  </td>
                  <td className="py-3 px-4 text-slate-500 max-w-[150px] truncate" title={e.source}>
                    {e.source}
                  </td>
                  <td className="py-3 px-4 text-slate-500 font-mono">
                    {e.createdAt ? new Date(e.createdAt).toLocaleString() : '—'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PiiEntityTable;
