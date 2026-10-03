import React from 'react';
import { History, CheckCircle2, XCircle, AlertCircle, Clock } from 'lucide-react';

const ScanHistoryTable = ({ history }) => {
  const getStatusIcon = (status) => {
    switch (status) {
      case 'COMPLETED': return <CheckCircle2 className="w-4 h-4 text-emerald-400" />;
      case 'FAILED': return <XCircle className="w-4 h-4 text-rose-400" />;
      case 'RUNNING': return <Clock className="w-4 h-4 text-cyan-400 animate-pulse" />;
      default: return <AlertCircle className="w-4 h-4 text-slate-400" />;
    }
  };

  const getStatusStyle = (status) => {
    switch (status) {
      case 'COMPLETED': return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
      case 'FAILED': return 'bg-rose-500/10 text-rose-400 border-rose-500/20';
      case 'RUNNING': return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20';
      default: return 'bg-slate-800 text-slate-400 border-slate-700';
    }
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  };

  const formatDuration = (seconds) => {
    if (seconds == null) return '-';
    if (seconds < 60) return `${seconds}s`;
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}m ${s}s`;
  };

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 lg:col-span-2">
      <h2 className="text-xl font-semibold text-white mb-6 flex items-center gap-2">
        <History className="w-5 h-5 text-blue-400" />
        Recent Scheduled Scans
      </h2>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-800">
              <th className="pb-3 text-xs font-semibold text-slate-400 uppercase">Started At</th>
              <th className="pb-3 text-xs font-semibold text-slate-400 uppercase">Type</th>
              <th className="pb-3 text-xs font-semibold text-slate-400 uppercase">Status</th>
              <th className="pb-3 text-xs font-semibold text-slate-400 uppercase">Duration</th>
              <th className="pb-3 text-xs font-semibold text-slate-400 uppercase">Records</th>
              <th className="pb-3 text-xs font-semibold text-slate-400 uppercase">PII Found</th>
            </tr>
          </thead>
          <tbody className="text-sm text-slate-300">
            {history.length === 0 ? (
              <tr>
                <td colSpan="6" className="py-8 text-center text-slate-500">
                  No scheduled scans have been run yet.
                </td>
              </tr>
            ) : (
              history.map((scan) => (
                <tr key={scan.id} className="border-b border-slate-800/50 hover:bg-slate-800/20 transition-colors">
                  <td className="py-4">
                    <div className="font-medium text-white">{formatDateTime(scan.startedAt)}</div>
                    <div className="text-xs text-slate-500">By: {scan.triggeredBy}</div>
                  </td>
                  <td className="py-4">
                    <span className="text-xs font-mono bg-slate-800 px-2 py-1 rounded text-slate-300">
                      {scan.scanType}
                    </span>
                  </td>
                  <td className="py-4">
                    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border ${getStatusStyle(scan.status)}`} title={scan.failedReason}>
                      {getStatusIcon(scan.status)}
                      {scan.status}
                    </div>
                  </td>
                  <td className="py-4 font-mono text-slate-400">{formatDuration(scan.duration)}</td>
                  <td className="py-4 font-mono text-slate-300">
                    {scan.recordsScanned != null ? scan.recordsScanned.toLocaleString() : '-'}
                  </td>
                  <td className="py-4 font-mono text-rose-400">
                    {scan.piiDetected != null ? scan.piiDetected.toLocaleString() : '-'}
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

export default ScanHistoryTable;
