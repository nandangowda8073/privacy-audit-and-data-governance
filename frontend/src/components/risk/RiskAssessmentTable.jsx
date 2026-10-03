import React, { useState } from 'react';
import { Search, Filter, ShieldAlert } from 'lucide-react';

const RISK_STYLES = {
  CRITICAL: 'bg-rose-500/20 text-rose-400 border-rose-500/40',
  HIGH:     'bg-amber-500/20 text-amber-400 border-amber-500/40',
  MEDIUM:   'bg-yellow-500/20 text-yellow-400 border-yellow-500/40',
  LOW:      'bg-emerald-500/20 text-emerald-400 border-emerald-500/40',
};

const RiskAssessmentTable = ({ assessments }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterRisk, setFilterRisk] = useState('ALL');

  const filtered = assessments.filter((item) => {
    const matchesSearch = item.piiType?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          item.source?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesRisk = filterRisk === 'ALL' || item.riskLevel === filterRisk;
    return matchesSearch && matchesRisk;
  });

  return (
    <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-800/80 pb-4">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-cyan-400" />
            Risk Classification Details
          </h2>
          <p className="text-xs text-slate-400 mt-1">Detailed breakdown of PII findings and their assigned risk score.</p>
        </div>
        
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <div className="relative flex-1 sm:flex-none">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 transform -translate-y-1/2" />
            <input 
              type="text"
              placeholder="Search type or source..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full sm:w-48 pl-9 pr-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-sm text-slate-200 placeholder-slate-500 focus:border-cyan-500 outline-none"
            />
          </div>
          <div className="relative">
            <Filter className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 transform -translate-y-1/2 pointer-events-none" />
            <select
              value={filterRisk}
              onChange={(e) => setFilterRisk(e.target.value)}
              className="pl-9 pr-8 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-sm text-slate-200 focus:border-cyan-500 outline-none appearance-none cursor-pointer"
            >
              <option value="ALL">All Risks</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
              <th className="p-3 font-medium">PII Type</th>
              <th className="p-3 font-medium">Risk Level</th>
              <th className="p-3 font-medium">Score</th>
              <th className="p-3 font-medium">Reason</th>
              <th className="p-3 font-medium">Source</th>
              <th className="p-3 font-medium">Column/Location</th>
              <th className="p-3 font-medium">Detected At</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/50">
            {filtered.length > 0 ? (
              filtered.map((item) => (
                <tr key={item.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="p-3">
                    <span className="font-mono text-xs text-slate-200 bg-slate-900 px-2 py-1 rounded border border-slate-700">
                      {item.piiType}
                    </span>
                  </td>
                  <td className="p-3">
                    <span className={`px-2.5 py-1 rounded-md text-[10px] font-bold tracking-wider border ${RISK_STYLES[item.riskLevel] || RISK_STYLES.LOW}`}>
                      {item.riskLevel}
                    </span>
                  </td>
                  <td className="p-3 text-sm text-slate-300 font-mono text-center">
                    {item.riskScore}
                  </td>
                  <td className="p-3 text-xs text-slate-400 max-w-xs truncate" title={item.reason}>
                    {item.reason}
                  </td>
                  <td className="p-3 text-xs text-slate-400">
                    {item.source}
                  </td>
                  <td className="p-3 text-xs text-slate-500 font-mono">
                    {item.columnName || '-'}
                  </td>
                  <td className="p-3 text-xs text-slate-500">
                    {new Date(item.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="7" className="p-8 text-center text-slate-500 text-sm">
                  No risk assessments found matching your criteria.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default RiskAssessmentTable;
