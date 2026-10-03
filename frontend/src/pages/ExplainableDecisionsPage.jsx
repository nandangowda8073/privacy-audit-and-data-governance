import React, { useState, useEffect, useCallback } from 'react';
import API from '../api/axios';
import { useAuth } from '../context/AuthContext';
import AccessDeniedPage from './AccessDeniedPage';
import { 
  HelpCircle, Search, RefreshCw, ShieldAlert, CheckCircle2, 
  AlertTriangle, Filter, ChevronLeft, ChevronRight, Info, User, Lock, Server, X,
  ArrowRight, KeyRound, Eye, ShieldCheck, Play
} from 'lucide-react';

export const ExplainableDecisionsPage = () => {
  const { hasPermission } = useAuth();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Pagination
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Selected Decision for Detail Modal
  const [selectedDecision, setSelectedDecision] = useState(null);

  // Live Explainer Tool Form
  const [explainType, setExplainType] = useState('RISK_CLASSIFICATION');
  const [piiEntityIdInput, setPiiEntityIdInput] = useState('1');
  const [reqPermInput, setReqPermInput] = useState('PII_VIEW');
  const [explaining, setExplaining] = useState(false);

  const fetchHistory = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await API.get('/v1/decisions/history', {
        params: { page, size }
      });
      if (response.data?.success) {
        const pageData = response.data.data;
        setHistory(pageData.content || []);
        setTotalPages(pageData.totalPages || 0);
        setTotalElements(pageData.totalElements || 0);
      }
    } catch (err) {
      console.error('Failed to fetch decision history:', err);
      setError(err.response?.data?.message || 'Failed to load decision history from server');
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  const handleExplainCurrent = async () => {
    if (!hasPermission('DECISION_EXPLAIN')) {
      alert('You do not have permission to generate decision explanations.');
      return;
    }

    setExplaining(true);
    try {
      const payload = {
        decisionType: explainType,
        piiEntityId: explainType !== 'ACCESS' ? LongNumber(piiEntityIdInput) : null,
        requiredPermission: explainType === 'ACCESS' ? reqPermInput : null
      };

      const res = await API.post('/v1/decisions/explain', payload);
      if (res.data?.success) {
        setSelectedDecision(res.data.data);
        fetchHistory(); // refresh history table
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to explain decision');
    } finally {
      setExplaining(false);
    }
  };

  const LongNumber = (val) => {
    const parsed = parseInt(val, 10);
    return isNaN(parsed) ? 1 : parsed;
  };

  if (!hasPermission('DECISION_VIEW')) {
    return <AccessDeniedPage message="You do not have permission to view Explainable Security & Privacy Decisions." />;
  }

  const getOutcomeBadge = (outcome) => {
    if (outcome === 'CRITICAL' || outcome === 'DENIED' || outcome === 'FULL' || outcome === 'FULL_MASK') {
      return 'bg-rose-500/20 text-rose-300 border-rose-500/30';
    } else if (outcome === 'HIGH' || outcome === 'PARTIAL' || outcome === 'PARTIAL_MASK') {
      return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
    } else if (outcome === 'ALLOWED' || outcome === 'LOW' || outcome === 'NONE' || outcome === 'NO_MASK') {
      return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30';
    }
    return 'bg-slate-800 text-slate-300 border-slate-700';
  };

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 bg-gradient-to-r from-[#0d1322] via-[#111827] to-[#0f172a] shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded text-[10px] font-mono bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 font-semibold uppercase tracking-wider">
              Deterministic & Rule-Based
            </span>
            <span className="text-xs font-mono text-slate-400">Zero Raw PII Exposure</span>
          </div>
          <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <HelpCircle className="w-7 h-7 text-cyan-400" />
            Explainable Security & Privacy Decisions
          </h1>
          <p className="text-xs text-slate-400 max-w-2xl font-mono">
            Auditable explanations detailing WHY risk levels, dynamic data masking types, and RBAC authorization access outcomes were determined.
          </p>
        </div>

        <button 
          onClick={fetchHistory}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-slate-200 text-xs font-mono transition-all self-start md:self-auto"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* On-the-Fly Decision Explainer Toolbar */}
      {hasPermission('DECISION_EXPLAIN') && (
        <div className="glass-panel p-5 rounded-2xl border border-slate-800 bg-slate-950/70 space-y-4">
          <div className="flex items-center gap-2 text-xs font-bold text-slate-200 font-mono">
            <Play className="w-4 h-4 text-emerald-400" />
            <span>Generate Decision Explanation</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 items-end">
            <div>
              <label className="block text-[10px] font-mono text-slate-400 uppercase tracking-wider mb-1">Decision Type</label>
              <select
                value={explainType}
                onChange={(e) => setExplainType(e.target.value)}
                className="w-full bg-slate-900 border border-slate-800 text-white text-xs font-mono px-3 py-2 rounded-xl focus:outline-none focus:border-cyan-500"
              >
                <option value="RISK_CLASSIFICATION">RISK CLASSIFICATION</option>
                <option value="MASKING">DYNAMIC MASKING</option>
                <option value="ACCESS">RBAC ACCESS CONTROL</option>
              </select>
            </div>

            {explainType !== 'ACCESS' ? (
              <div>
                <label className="block text-[10px] font-mono text-slate-400 uppercase tracking-wider mb-1">PII Entity ID</label>
                <input
                  type="number"
                  value={piiEntityIdInput}
                  onChange={(e) => setPiiEntityIdInput(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 text-white text-xs font-mono px-3 py-2 rounded-xl focus:outline-none focus:border-cyan-500"
                  placeholder="e.g. 1"
                />
              </div>
            ) : (
              <div>
                <label className="block text-[10px] font-mono text-slate-400 uppercase tracking-wider mb-1">Required Permission</label>
                <select
                  value={reqPermInput}
                  onChange={(e) => setReqPermInput(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 text-white text-xs font-mono px-3 py-2 rounded-xl focus:outline-none focus:border-cyan-500"
                >
                  <option value="PII_VIEW">PII_VIEW</option>
                  <option value="PII_EXPORT">PII_EXPORT</option>
                  <option value="RISK_VIEW">RISK_VIEW</option>
                  <option value="MASKING_POLICY_VIEW">MASKING_POLICY_VIEW</option>
                  <option value="REPORT_VIEW">REPORT_VIEW</option>
                  <option value="DECISION_VIEW">DECISION_VIEW</option>
                  <option value="DECISION_EXPLAIN">DECISION_EXPLAIN</option>
                </select>
              </div>
            )}

            <div className="sm:col-span-2">
              <button
                onClick={handleExplainCurrent}
                disabled={explaining}
                className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white text-xs font-mono font-bold rounded-xl shadow-lg transition-all"
              >
                <HelpCircle className="w-4 h-4" />
                {explaining ? 'Analyzing Decision Rules...' : 'Explain Current Decision'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Decision History Table */}
      <div className="glass-panel rounded-3xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="p-5 border-b border-slate-800 flex justify-between items-center bg-slate-950/40">
          <div>
            <h3 className="text-sm font-bold text-white font-mono">Decision History & Audit Traces</h3>
            <p className="text-[11px] text-slate-400 font-mono">Showing {totalElements} recorded decision events</p>
          </div>
        </div>

        {loading ? (
          <div className="p-12 text-center text-slate-400 font-mono text-xs">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-cyan-400" />
            Loading explainable decision history...
          </div>
        ) : error ? (
          <div className="p-8 text-center text-rose-400 font-mono text-xs bg-rose-500/5">
            <AlertTriangle className="w-6 h-6 mx-auto mb-2 text-rose-400" />
            {error}
          </div>
        ) : history.length === 0 ? (
          <div className="p-12 text-center text-slate-500 font-mono text-xs">
            No decision events found in system logs.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[10px] font-mono uppercase tracking-wider text-slate-400 bg-slate-950/70">
                  <th className="py-3.5 px-4">Timestamp</th>
                  <th className="py-3.5 px-4">User & Role</th>
                  <th className="py-3.5 px-4">Type</th>
                  <th className="py-3.5 px-4">Outcome</th>
                  <th className="py-3.5 px-4">PII & Risk</th>
                  <th className="py-3.5 px-4">Policy / Permission</th>
                  <th className="py-3.5 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
                {history.map((item, idx) => (
                  <tr key={item.decisionId || idx} className="hover:bg-slate-900/40 transition-colors">
                    <td className="py-3 px-4 text-slate-400 text-[11px]">
                      {item.timestamp ? new Date(item.timestamp).toLocaleString() : 'N/A'}
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-semibold text-slate-200">{item.username || 'SYSTEM'}</div>
                      <div className="text-[10px] text-slate-400">{item.role || 'ROLE_USER'}</div>
                    </td>
                    <td className="py-3 px-4 font-semibold text-cyan-400">
                      {item.decisionType}
                    </td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getOutcomeBadge(item.outcome)}`}>
                        {item.outcome}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <div className="text-slate-300 font-semibold">{item.piiType || '—'}</div>
                      <div className="text-[10px] text-slate-400">{item.riskLevel || '—'}</div>
                    </td>
                    <td className="py-3 px-4 text-slate-300 text-[11px]">
                      {item.policyName || item.requiredPermission || 'Standard Rule'}
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => setSelectedDecision(item)}
                        className="px-3 py-1 rounded-lg bg-cyan-500/10 text-cyan-300 hover:bg-cyan-500/20 border border-cyan-500/30 text-[11px] font-bold transition-all"
                      >
                        View Explanation
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-slate-800 flex items-center justify-between text-xs font-mono text-slate-400 bg-slate-950/40">
            <div>
              Page {page + 1} of {totalPages} ({totalElements} total items)
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="p-1.5 rounded-lg bg-slate-900 border border-slate-800 text-slate-300 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="p-1.5 rounded-lg bg-slate-900 border border-slate-800 text-slate-300 disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Decision Detail & Trace Modal */}
      {selectedDecision && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="glass-panel p-6 rounded-3xl border border-slate-800 max-w-2xl w-full bg-[#0b0f19] space-y-6 shadow-2xl relative">
            <button
              onClick={() => setSelectedDecision(null)}
              className="absolute top-5 right-5 text-slate-400 hover:text-white p-1 rounded-lg bg-slate-900 border border-slate-800"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="space-y-1">
              <div className="flex items-center gap-2 font-mono text-xs text-cyan-400 font-semibold">
                <ShieldCheck className="w-4 h-4" />
                <span>Decision Explanation Breakdown</span>
                {selectedDecision.isHistorical && (
                  <span className="px-2 py-0.5 rounded text-[9px] bg-purple-500/20 text-purple-300 border border-purple-500/30">
                    HISTORICAL AUDIT EVENT
                  </span>
                )}
              </div>
              <h2 className="text-xl font-extrabold text-white">
                Decision: <span className="text-cyan-300">{selectedDecision.outcome}</span>
              </h2>
            </div>

            {/* Explanation Summary Box */}
            <div className="p-4 rounded-2xl bg-cyan-500/10 border border-cyan-500/20 space-y-2">
              <div className="text-xs font-mono font-bold text-cyan-300 uppercase tracking-wider">
                Human-Readable Reason (WHY?)
              </div>
              <p className="text-sm font-mono text-slate-200 leading-relaxed">
                "{selectedDecision.reason}"
              </p>
            </div>

            {/* Decision Factors */}
            {selectedDecision.factors && selectedDecision.factors.length > 0 && (
              <div className="space-y-2">
                <div className="text-xs font-mono uppercase tracking-wider text-slate-400 font-semibold">
                  Deterministic Decision Factors
                </div>
                <div className="space-y-1.5">
                  {selectedDecision.factors.map((factor, fIdx) => (
                    <div key={fIdx} className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800/80 text-xs font-mono text-slate-300 flex items-center gap-2">
                      <CheckCircle2 className="w-3.5 h-3.5 text-cyan-400 flex-shrink-0" />
                      <span>{factor}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Step-by-Step Decision Trace */}
            {selectedDecision.decisionTrace && selectedDecision.decisionTrace.length > 0 && (
              <div className="space-y-2">
                <div className="text-xs font-mono uppercase tracking-wider text-slate-400 font-semibold">
                  Execution Trace Flow
                </div>
                <div className="p-4 rounded-2xl bg-slate-950 border border-slate-800 space-y-2 font-mono text-xs text-slate-300">
                  {selectedDecision.decisionTrace.map((step, sIdx) => (
                    <div key={sIdx} className="flex items-start gap-2">
                      <ArrowRight className="w-3.5 h-3.5 text-cyan-400 mt-0.5 flex-shrink-0" />
                      <span>{step}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Metadata Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 font-mono text-xs">
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800">
                <div className="text-[10px] text-slate-500">User / Role</div>
                <div className="font-semibold text-slate-200">{selectedDecision.username || 'N/A'}</div>
                <div className="text-[10px] text-cyan-400">{selectedDecision.role || 'ROLE_USER'}</div>
              </div>
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800">
                <div className="text-[10px] text-slate-500">PII Type</div>
                <div className="font-semibold text-slate-200">{selectedDecision.piiType || 'N/A'}</div>
                <div className="text-[10px] text-amber-400">{selectedDecision.riskLevel || 'N/A'}</div>
              </div>
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800">
                <div className="text-[10px] text-slate-500">Correlation ID</div>
                <div className="font-semibold text-cyan-300 text-[11px] truncate">
                  {selectedDecision.correlationId || selectedDecision.decisionId}
                </div>
              </div>
            </div>

            <div className="pt-2 text-right">
              <button
                onClick={() => setSelectedDecision(null)}
                className="px-5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-mono font-bold transition-all"
              >
                Close Explanation
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
