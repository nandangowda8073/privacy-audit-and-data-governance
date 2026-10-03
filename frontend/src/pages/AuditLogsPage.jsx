import React, { useState, useEffect, useCallback } from 'react';
import API from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { 
  FileText, Search, Download, RefreshCw, ShieldAlert, CheckCircle2, 
  AlertTriangle, Filter, ChevronLeft, ChevronRight, Info, User, Lock, Server, X
} from 'lucide-react';

const AuditLogsPage = () => {
  const { hasPermission } = useAuth();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Pagination
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [search, setSearch] = useState('');
  const [actionFilter, setActionFilter] = useState('');
  const [resultFilter, setResultFilter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');

  // Selected Log for Detail Modal
  const [selectedLog, setSelectedLog] = useState(null);
  const [isExporting, setIsExporting] = useState(false);

  const fetchAuditLogs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        page,
        size,
        sortBy: 'timestamp',
        direction: 'desc'
      };

      if (search) params.search = search;
      if (actionFilter) params.action = actionFilter;
      if (resultFilter) params.result = resultFilter;
      if (roleFilter) params.role = roleFilter;

      const response = await API.get('/audit-logs', { params });
      if (response.data && response.data.success) {
        const pageData = response.data.data;
        setLogs(pageData.content || []);
        setTotalPages(pageData.totalPages || 0);
        setTotalElements(pageData.totalElements || 0);
      }
    } catch (err) {
      console.error('Failed to fetch audit logs:', err);
      setError(err.response?.data?.message || 'Failed to load audit logs from server');
    } finally {
      setLoading(false);
    }
  }, [page, size, search, actionFilter, resultFilter, roleFilter]);

  useEffect(() => {
    fetchAuditLogs();
  }, [fetchAuditLogs]);

  const handleExportCsv = async () => {
    if (!hasPermission('AUDIT_LOG_EXPORT')) {
      alert('You do not have permission to export audit logs');
      return;
    }

    setIsExporting(true);
    try {
      const params = {};
      if (search) params.search = search;
      if (actionFilter) params.action = actionFilter;
      if (resultFilter) params.result = resultFilter;
      if (roleFilter) params.role = roleFilter;

      const response = await API.get('/audit-logs/export', {
        params,
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `audit_logs_${new Date().toISOString().slice(0, 10)}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error('Export failed:', err);
      alert('Failed to export audit logs CSV');
    } finally {
      setIsExporting(false);
    }
  };

  const resetFilters = () => {
    setSearch('');
    setActionFilter('');
    setResultFilter('');
    setRoleFilter('');
    setPage(0);
  };

  const getResultBadge = (result) => {
    switch (result) {
      case 'SUCCESS':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono font-bold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            <CheckCircle2 className="w-3 h-3" /> SUCCESS
          </span>
        );
      case 'DENIED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono font-bold bg-amber-500/10 border border-amber-500/30 text-amber-400">
            <ShieldAlert className="w-3 h-3" /> DENIED
          </span>
        );
      case 'FAILURE':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono font-bold bg-rose-500/10 border border-rose-500/30 text-rose-400">
            <AlertTriangle className="w-3 h-3" /> FAILURE
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-mono text-slate-400 bg-slate-800 border border-slate-700">
            {result}
          </span>
        );
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    try {
      const date = new Date(isoString);
      return date.toLocaleString('en-US', {
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: true
      });
    } catch {
      return isoString;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <FileText className="w-6 h-6 text-cyan-400" />
            <h1 className="text-xl font-bold text-white tracking-wide">Enterprise Audit Logs</h1>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Centralized immutable historical log trail for compliance, security monitoring, and incident investigation.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={fetchAuditLogs}
            disabled={loading}
            className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-semibold bg-slate-800 border border-slate-700 text-slate-300 hover:bg-slate-700 hover:text-white transition-all disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </button>

          {hasPermission('AUDIT_LOG_EXPORT') && (
            <button
              onClick={handleExportCsv}
              disabled={isExporting}
              className="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold bg-gradient-to-r from-cyan-500 to-blue-600 text-white shadow-lg shadow-cyan-500/20 hover:opacity-90 transition-all disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5" />
              {isExporting ? 'Exporting...' : 'Export CSV'}
            </button>
          )}
        </div>
      </div>

      {/* Filter Bar */}
      <div className="glass-panel p-4 rounded-2xl border border-slate-800 space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
          {/* Search Box */}
          <div className="relative md:col-span-2">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Search user, action, IP, Request ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-slate-900/80 border border-slate-800 rounded-xl pl-9 pr-4 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500/50"
            />
          </div>

          {/* Action Selector */}
          <div>
            <select
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
              className="w-full bg-slate-900/80 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-cyan-500/50"
            >
              <option value="">All Actions</option>
              <option value="AUTH_LOGIN">AUTH_LOGIN</option>
              <option value="AUTH_LOGIN_FAILED">AUTH_LOGIN_FAILED</option>
              <option value="USER_CREATE">USER_CREATE</option>
              <option value="USER_ROLE_CHANGED">USER_ROLE_CHANGED</option>
              <option value="ROLE_PERMISSION_UPDATED">ROLE_PERMISSION_UPDATED</option>
              <option value="DATABASE_CREATE">DATABASE_CREATE</option>
              <option value="DATABASE_CONNECTION_TEST">DATABASE_CONNECTION_TEST</option>
              <option value="SCAN_STARTED">SCAN_STARTED</option>
              <option value="SCAN_COMPLETED">SCAN_COMPLETED</option>
              <option value="PII_DETECTED">PII_DETECTED</option>
              <option value="RISK_CLASSIFIED">RISK_CLASSIFIED</option>
              <option value="MASKING_APPLIED">MASKING_APPLIED</option>
              <option value="ACCESS_DENIED">ACCESS_DENIED</option>
            </select>
          </div>

          {/* Result Selector */}
          <div>
            <select
              value={resultFilter}
              onChange={(e) => setResultFilter(e.target.value)}
              className="w-full bg-slate-900/80 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-cyan-500/50"
            >
              <option value="">All Results</option>
              <option value="SUCCESS">SUCCESS</option>
              <option value="DENIED">DENIED</option>
              <option value="FAILURE">FAILURE</option>
            </select>
          </div>

          {/* Role Selector */}
          <div>
            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              className="w-full bg-slate-900/80 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-cyan-500/50"
            >
              <option value="">All Roles</option>
              <option value="ROLE_SUPER_ADMIN">SUPER_ADMIN</option>
              <option value="ROLE_SECURITY_OFFICER">SECURITY_OFFICER</option>
              <option value="ROLE_AUDITOR">AUDITOR</option>
              <option value="ROLE_VIEWER">VIEWER</option>
              <option value="SYSTEM">SYSTEM</option>
            </select>
          </div>
        </div>

        {(search || actionFilter || resultFilter || roleFilter) && (
          <div className="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-800">
            <span>Showing filtered results</span>
            <button
              onClick={resetFilters}
              className="text-cyan-400 hover:underline text-xs font-semibold"
            >
              Reset All Filters
            </button>
          </div>
        )}
      </div>

      {/* Main Table */}
      <div className="glass-panel rounded-3xl border border-slate-800 overflow-hidden">
        {loading ? (
          <div className="py-20 text-center space-y-3">
            <RefreshCw className="w-8 h-8 text-cyan-400 animate-spin mx-auto" />
            <p className="text-xs text-slate-400 font-mono">Querying audit logs database...</p>
          </div>
        ) : error ? (
          <div className="py-16 text-center space-y-3">
            <AlertTriangle className="w-8 h-8 text-rose-400 mx-auto" />
            <p className="text-sm font-semibold text-rose-400">{error}</p>
          </div>
        ) : logs.length === 0 ? (
          <div className="py-16 text-center space-y-3">
            <Info className="w-8 h-8 text-slate-500 mx-auto" />
            <p className="text-sm font-semibold text-slate-400">No audit log records match your current filters.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 text-[11px] uppercase tracking-wider bg-slate-900/60">
                  <th className="py-3.5 px-4">Time</th>
                  <th className="py-3.5 px-4">User & Role</th>
                  <th className="py-3.5 px-4">Action</th>
                  <th className="py-3.5 px-4">Resource</th>
                  <th className="py-3.5 px-4">Result</th>
                  <th className="py-3.5 px-4">IP / Req ID</th>
                  <th className="py-3.5 px-4 text-right">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {logs.map((item) => (
                  <tr 
                    key={item.id} 
                    onClick={() => setSelectedLog(item)}
                    className="hover:bg-slate-900/50 transition-colors cursor-pointer"
                  >
                    <td className="py-3.5 px-4 text-cyan-400 font-semibold whitespace-nowrap">
                      {formatDate(item.timestamp)}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <div>
                        <span className="text-white font-medium block">{item.username}</span>
                        <span className="text-[10px] text-slate-500">{item.role || 'N/A'}</span>
                      </div>
                    </td>
                    <td className="py-3.5 px-4 text-slate-200 font-semibold">
                      {item.action}
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      <span className="text-slate-400">{item.resourceType}:</span> {item.resourceId || 'N/A'}
                    </td>
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {getResultBadge(item.result)}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400 whitespace-nowrap text-[11px]">
                      <div>{item.ipAddress || '127.0.0.1'}</div>
                      <div className="text-[10px] text-slate-600 font-mono">{item.requestId || 'REQ-SYS'}</div>
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button 
                        onClick={(e) => { e.stopPropagation(); setSelectedLog(item); }}
                        className="text-cyan-400 hover:text-cyan-300 text-xs font-semibold px-2 py-1 bg-cyan-500/10 rounded-lg border border-cyan-500/20"
                      >
                        Inspect
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Bar */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400 bg-slate-900/40 font-mono">
            <div>
              Showing {logs.length} of {totalElements} log entries (Page {page + 1} of {totalPages})
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="p-1.5 rounded-lg bg-slate-800 border border-slate-700 text-slate-300 hover:bg-slate-700 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span>{page + 1} / {totalPages}</span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="p-1.5 rounded-lg bg-slate-800 border border-slate-700 text-slate-300 hover:bg-slate-700 disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Log Detail Modal */}
      {selectedLog && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="glass-panel max-w-2xl w-full p-6 rounded-3xl border border-slate-800 space-y-6 shadow-2xl animate-fade-in">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-3">
                <div className="p-2.5 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400">
                  <FileText className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">Audit Event Details</h3>
                  <p className="text-xs text-slate-400 font-mono">Log ID #{selectedLog.id} — {selectedLog.requestId}</p>
                </div>
              </div>
              <button 
                onClick={() => setSelectedLog(null)}
                className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Zero PII Assurance Tag */}
            <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center gap-2.5 text-xs text-emerald-400 font-mono">
              <Lock className="w-4 h-4 shrink-0" />
              <span>Security Guarantee: Metadata log only. Raw PII values, credentials, and tokens are strictly unrecorded.</span>
            </div>

            {/* Detail Grid */}
            <div className="grid grid-cols-2 gap-4 text-xs font-mono">
              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                <span className="text-slate-500 uppercase text-[10px]">Action</span>
                <p className="text-cyan-400 font-bold">{selectedLog.action}</p>
              </div>

              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                <span className="text-slate-500 uppercase text-[10px]">Result</span>
                <div>{getResultBadge(selectedLog.result)}</div>
              </div>

              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                <span className="text-slate-500 uppercase text-[10px]">User & Role</span>
                <p className="text-white font-medium">{selectedLog.username}</p>
                <p className="text-slate-400 text-[10px]">{selectedLog.role}</p>
              </div>

              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                <span className="text-slate-500 uppercase text-[10px]">Timestamp</span>
                <p className="text-slate-200">{formatDate(selectedLog.timestamp)}</p>
              </div>

              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                <span className="text-slate-500 uppercase text-[10px]">Client IP</span>
                <p className="text-slate-200">{selectedLog.ipAddress || '127.0.0.1'}</p>
              </div>

              <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                <span className="text-slate-500 uppercase text-[10px]">Correlation Request ID</span>
                <p className="text-slate-200">{selectedLog.requestId || 'N/A'}</p>
              </div>

              <div className="col-span-2 p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                <span className="text-slate-500 uppercase text-[10px]">Description</span>
                <p className="text-slate-200 font-sans text-xs">{selectedLog.description || 'No description provided'}</p>
              </div>

              {selectedLog.reason && (
                <div className="col-span-2 p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-1">
                  <span className="text-slate-500 uppercase text-[10px]">Reason / Justification</span>
                  <p className="text-amber-300 font-sans text-xs">{selectedLog.reason}</p>
                </div>
              )}

              {/* Domain Metadata if present */}
              {(selectedLog.piiType || selectedLog.riskLevel || selectedLog.maskingType || selectedLog.databaseName) && (
                <div className="col-span-2 p-3 bg-slate-900/60 rounded-xl border border-slate-800 space-y-2">
                  <span className="text-slate-500 uppercase text-[10px]">Domain Specific Metadata</span>
                  <div className="grid grid-cols-2 gap-2 text-[11px]">
                    {selectedLog.piiType && <div><span className="text-slate-400">PII Entity:</span> <span className="text-cyan-400">{selectedLog.piiType}</span></div>}
                    {selectedLog.riskLevel && <div><span className="text-slate-400">Risk Level:</span> <span className="text-amber-400">{selectedLog.riskLevel}</span></div>}
                    {selectedLog.maskingType && <div><span className="text-slate-400">Masking Strategy:</span> <span className="text-purple-400">{selectedLog.maskingType}</span></div>}
                    {selectedLog.databaseName && <div><span className="text-slate-400">Target Database:</span> <span className="text-emerald-400">{selectedLog.databaseName}</span></div>}
                  </div>
                </div>
              )}
            </div>

            <div className="pt-2 flex justify-end">
              <button
                onClick={() => setSelectedLog(null)}
                className="px-5 py-2 rounded-xl text-xs font-semibold bg-slate-800 text-slate-300 hover:bg-slate-700 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AuditLogsPage;
