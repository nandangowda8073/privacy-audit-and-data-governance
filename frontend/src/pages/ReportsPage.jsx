import React, { useState, useEffect, useCallback } from 'react';
import API from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { 
  FileText, Download, RefreshCw, Filter, ShieldAlert, CheckCircle2, 
  AlertTriangle, Eye, Database, FileSpreadsheet, FileCode, Play, ChevronLeft, ChevronRight, X, Lock, Sparkles
} from 'lucide-react';

export const ReportsPage = () => {
  const { hasPermission } = useAuth();

  const [reports, setReports] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [error, setError] = useState(null);

  // Pagination
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Generation Modal & Form
  const [selectedReportType, setSelectedReportType] = useState(null);
  const [isGenerateModalOpen, setIsGenerateModalOpen] = useState(false);
  const [format, setFormat] = useState('PDF');
  const [dateFrom, setDateFrom] = useState(new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10));
  const [dateTo, setDateTo] = useState(new Date().toISOString().slice(0, 10));
  const [databaseId, setDatabaseId] = useState('');
  const [databases, setDatabases] = useState([]);

  // Preview state
  const [preview, setPreview] = useState(null);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [generating, setGenerating] = useState(false);

  const reportCards = [
    {
      type: 'SCAN_SUMMARY',
      title: 'Scan Summary Report',
      description: 'Overall executive summary of database scan activities, records scanned, and findings count.',
      icon: Database,
      color: 'from-cyan-500/20 to-blue-500/10 border-cyan-500/30 text-cyan-400'
    },
    {
      type: 'PII_DETECTION',
      title: 'PII Detection Inventory',
      description: 'Granular inventory of identified PII entities (Aadhaar, PAN, Cards, Emails) across connected tables.',
      icon: ShieldAlert,
      color: 'from-purple-500/20 to-indigo-500/10 border-purple-500/30 text-purple-400'
    },
    {
      type: 'RISK_ASSESSMENT',
      title: 'Risk Assessment Report',
      description: 'Privacy threat score calculations, risk level distribution (Critical to Low), and threat analysis.',
      icon: AlertTriangle,
      color: 'from-rose-500/20 to-amber-500/10 border-rose-500/30 text-rose-400'
    },
    {
      type: 'COMPLIANCE',
      title: 'Compliance Assessment',
      description: 'Regulatory posture evaluation against GDPR, CCPA, and DPDP Act governance policies.',
      icon: CheckCircle2,
      color: 'from-emerald-500/20 to-teal-500/10 border-emerald-500/30 text-emerald-400'
    },
    {
      type: 'AUDIT_LOG',
      title: 'Audit Event Log Report',
      description: 'Immutable historical audit trail of security actions, user activity, and authorization events.',
      icon: FileText,
      color: 'from-amber-500/20 to-yellow-500/10 border-amber-500/30 text-amber-400'
    },
    {
      type: 'DATABASE_RISK',
      title: 'Database Risk Matrix',
      description: 'Infrastructure risk profiling showing table scanning progress and unencrypted risk hotspots.',
      icon: Database,
      color: 'from-indigo-500/20 to-blue-500/10 border-indigo-500/30 text-indigo-400'
    }
  ];

  const fetchDatabases = async () => {
    try {
      const res = await API.get('/databases');
      if (res.data?.success) {
        setDatabases(res.data.data || []);
      }
    } catch {
      // Fallback
    }
  };

  const fetchReportHistory = useCallback(async () => {
    setLoadingHistory(true);
    setError(null);
    try {
      const res = await API.get('/reports', {
        params: { page, size, sortBy: 'generatedAt', direction: 'desc' }
      });
      if (res.data?.success) {
        const pageData = res.data.data;
        setReports(pageData.content || []);
        setTotalPages(pageData.totalPages || 0);
        setTotalElements(pageData.totalElements || 0);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load historical reports');
    } finally {
      setLoadingHistory(false);
    }
  }, [page, size]);

  useEffect(() => {
    fetchReportHistory();
    fetchDatabases();
  }, [fetchReportHistory]);

  const handleOpenGenerateModal = (card) => {
    setSelectedReportType(card.type);
    setIsGenerateModalOpen(true);
    setPreview(null);
  };

  const handleFetchPreview = async () => {
    if (!selectedReportType) return;
    setLoadingPreview(true);
    try {
      const req = {
        reportType: selectedReportType,
        format,
        dateFrom,
        dateTo,
        databaseId: databaseId ? parseInt(databaseId) : null
      };
      const res = await API.post('/reports/preview', req);
      if (res.data?.success) {
        setPreview(res.data.data);
      }
    } catch (err) {
      console.error('Preview error:', err);
    } finally {
      setLoadingPreview(false);
    }
  };

  const handleGenerateReport = async () => {
    if (!hasPermission('REPORT_GENERATE')) {
      alert('You do not have permission to generate reports.');
      return;
    }
    setGenerating(true);
    try {
      const req = {
        reportType: selectedReportType,
        format,
        dateFrom,
        dateTo,
        databaseId: databaseId ? parseInt(databaseId) : null
      };
      const res = await API.post('/reports/generate', req);
      if (res.data?.success) {
        setIsGenerateModalOpen(false);
        fetchReportHistory();
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to generate report');
    } finally {
      setGenerating(false);
    }
  };

  const handleDownloadReport = async (reportId, fileName) => {
    if (!hasPermission('REPORT_DOWNLOAD')) {
      alert('You do not have permission to download reports.');
      return;
    }
    try {
      const response = await API.get(`/reports/${reportId}/download`, {
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName || `report_${reportId}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Download failed. Ensure you have proper authorizations.');
    }
  };

  const getFormatBadge = (fmt) => {
    if (fmt === 'PDF') {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-rose-500/10 border border-rose-500/30 text-rose-400">
          <FileText className="w-3 h-3" /> PDF
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
        <FileSpreadsheet className="w-3 h-3" /> CSV
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <Sparkles className="w-6 h-6 text-cyan-400" />
            <h1 className="text-xl font-bold text-white tracking-wide">Enterprise Reporting & Governance</h1>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Centralized PDF and CSV report generation for executive summaries, PII inventories, risk assessments, and compliance audits.
          </p>
        </div>

        <button
          onClick={fetchReportHistory}
          disabled={loadingHistory}
          className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-semibold bg-slate-800 border border-slate-700 text-slate-300 hover:bg-slate-700 hover:text-white transition-all"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loadingHistory ? 'animate-spin' : ''}`} /> Refresh History
        </button>
      </div>

      {/* 6 Report Type Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {reportCards.map((card) => {
          const Icon = card.icon;
          return (
            <div 
              key={card.type}
              className={`glass-panel p-5 rounded-2xl border bg-gradient-to-br ${card.color} flex flex-col justify-between space-y-4 hover:border-cyan-500/50 transition-all group`}
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="p-2.5 rounded-xl bg-slate-900/80 border border-slate-800 text-cyan-400">
                    <Icon className="w-5 h-5" />
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="text-[10px] font-mono bg-slate-900/80 border border-slate-800 px-2 py-0.5 rounded text-slate-400">PDF</span>
                    <span className="text-[10px] font-mono bg-slate-900/80 border border-slate-800 px-2 py-0.5 rounded text-slate-400">CSV</span>
                  </div>
                </div>

                <h3 className="text-base font-bold text-white group-hover:text-cyan-300 transition-colors">{card.title}</h3>
                <p className="text-xs text-slate-400 leading-relaxed">{card.description}</p>
              </div>

              {hasPermission('REPORT_GENERATE') ? (
                <button
                  onClick={() => handleOpenGenerateModal(card)}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold bg-cyan-500/20 border border-cyan-500/40 text-cyan-300 hover:bg-cyan-500/30 transition-all"
                >
                  <Play className="w-3.5 h-3.5 fill-current" /> Configure & Generate
                </button>
              ) : (
                <span className="text-[11px] text-slate-500 font-mono text-center">View-only role</span>
              )}
            </div>
          );
        })}
      </div>

      {/* Report History Archive */}
      <div className="glass-panel rounded-3xl border border-slate-800 overflow-hidden space-y-4 p-6">
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <div>
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <FileText className="w-5 h-5 text-amber-400" />
              Generated Report History Archive
            </h2>
            <p className="text-xs text-slate-400">Historical trail of generated compliance and risk reports</p>
          </div>
        </div>

        {loadingHistory ? (
          <div className="py-12 text-center space-y-2">
            <RefreshCw className="w-6 h-6 text-cyan-400 animate-spin mx-auto" />
            <p className="text-xs text-slate-400 font-mono">Querying report history records...</p>
          </div>
        ) : error ? (
          <div className="py-12 text-center text-rose-400 text-xs font-semibold">{error}</div>
        ) : reports.length === 0 ? (
          <div className="py-12 text-center text-slate-500 text-xs font-mono">No reports generated yet. Select a report card above to generate one.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 text-[11px] uppercase bg-slate-900/60">
                  <th className="py-3 px-4">Report ID</th>
                  <th className="py-3 px-4">Report Type</th>
                  <th className="py-3 px-4">Format</th>
                  <th className="py-3 px-4">Generated By</th>
                  <th className="py-3 px-4">Generated At</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {reports.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-900/40">
                    <td className="py-3 px-4 text-cyan-400 font-bold">#{item.id}</td>
                    <td className="py-3 px-4 text-white font-semibold">{item.reportType}</td>
                    <td className="py-3 px-4">{getFormatBadge(item.format)}</td>
                    <td className="py-3 px-4 text-slate-300">{item.generatedBy}</td>
                    <td className="py-3 px-4 text-slate-400">{item.generatedAt ? item.generatedAt.replace('T', ' ').slice(0, 19) : 'N/A'}</td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${
                        item.status === 'COMPLETED' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' :
                        item.status === 'FAILED' ? 'bg-rose-500/10 border-rose-500/30 text-rose-400' :
                        'bg-amber-500/10 border-amber-500/30 text-amber-400'
                      }`}>
                        {item.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      {item.status === 'COMPLETED' && hasPermission('REPORT_DOWNLOAD') && (
                        <button
                          onClick={() => handleDownloadReport(item.id, item.fileName)}
                          className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-semibold bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 hover:bg-cyan-500/20 ml-auto"
                        >
                          <Download className="w-3.5 h-3.5" /> Download
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="p-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400 font-mono">
            <div>Showing {reports.length} of {totalElements} report records</div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="p-1 rounded bg-slate-800 text-slate-300 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span>{page + 1} / {totalPages}</span>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="p-1 rounded bg-slate-800 text-slate-300 disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Generate Report Configuration Modal */}
      {isGenerateModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="glass-panel max-w-xl w-full p-6 rounded-3xl border border-slate-800 space-y-6 shadow-2xl animate-fade-in">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-2.5">
                <div className="p-2 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400">
                  <FileText className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">Generate {selectedReportType} Report</h3>
                  <p className="text-xs text-slate-400 font-mono">Configure parameter filters and select output format</p>
                </div>
              </div>
              <button onClick={() => setIsGenerateModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Zero Raw PII Guarantee Tag */}
            <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center gap-2 text-xs text-emerald-400 font-mono">
              <Lock className="w-4 h-4 shrink-0" />
              <span>Zero PII Policy: Only metadata, risk scores, and counts are exported.</span>
            </div>

            {/* Form Fields */}
            <div className="grid grid-cols-2 gap-4 text-xs">
              <div>
                <label className="block text-slate-400 mb-1 font-mono">Output Format</label>
                <select
                  value={format}
                  onChange={(e) => setFormat(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-cyan-500"
                >
                  <option value="PDF">PDF (Enterprise Document)</option>
                  <option value="CSV">CSV (Machine Readable Raw)</option>
                </select>
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-mono">Target Database</label>
                <select
                  value={databaseId}
                  onChange={(e) => setDatabaseId(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-cyan-500"
                >
                  <option value="">All Connected Databases</option>
                  {databases.map((db) => (
                    <option key={db.id} value={db.id}>{db.connectionName}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-mono">Date From</label>
                <input
                  type="date"
                  value={dateFrom}
                  onChange={(e) => setDateFrom(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-mono">Date To</label>
                <input
                  type="date"
                  value={dateTo}
                  onChange={(e) => setDateTo(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-cyan-500"
                />
              </div>
            </div>

            {/* Live Preview Button & Metadata Box */}
            <div className="space-y-3 pt-2 border-t border-slate-800">
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400 font-mono">Metadata Preview</span>
                <button
                  type="button"
                  onClick={handleFetchPreview}
                  disabled={loadingPreview}
                  className="text-xs text-cyan-400 hover:underline flex items-center gap-1"
                >
                  <Eye className="w-3.5 h-3.5" /> {loadingPreview ? 'Calculating...' : 'Preview Metrics'}
                </button>
              </div>

              {preview && (
                <div className="p-3 bg-slate-900/80 rounded-xl border border-slate-800 space-y-2 text-xs font-mono">
                  <div className="flex justify-between text-slate-300">
                    <span>Title:</span> <span className="text-cyan-400 font-bold">{preview.title}</span>
                  </div>
                  <div className="flex justify-between text-slate-300">
                    <span>Total Findings:</span> <span className="text-amber-400 font-bold">{preview.totalFindings}</span>
                  </div>
                  <div className="flex justify-between text-slate-300">
                    <span>Estimated Size:</span> <span>{preview.estimatedSize}</span>
                  </div>
                </div>
              )}
            </div>

            {/* Modal Actions */}
            <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
              <button
                type="button"
                onClick={() => setIsGenerateModalOpen(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold bg-slate-800 text-slate-300 hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleGenerateReport}
                disabled={generating}
                className="flex items-center gap-2 px-5 py-2 rounded-xl text-xs font-semibold bg-gradient-to-r from-cyan-500 to-blue-600 text-white shadow-lg shadow-cyan-500/20 hover:opacity-90 transition-all disabled:opacity-50"
              >
                <Play className="w-3.5 h-3.5 fill-current" />
                {generating ? 'Generating...' : 'Generate & Save Report'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportsPage;
