import React from 'react';
import { Database, ShieldAlert, CheckCircle2, RefreshCw, AlertTriangle } from 'lucide-react';

export const ScanResultsTable = ({ scans }) => {
  const defaultScans = [
    { id: 's1', database: 'Customer_DB', tablesScanned: 48, recordsScanned: 4500000, piiFound: 48200, riskLevel: 'HIGH', status: 'COMPLETED', timestamp: '02-Aug-2026 09:20 AM' },
    { id: 's2', database: 'Payment_Vault', tablesScanned: 12, recordsScanned: 1200000, piiFound: 32100, riskLevel: 'CRITICAL', status: 'IN_PROGRESS', timestamp: '02-Aug-2026 08:45 AM' },
    { id: 's3', database: 'HR_Master_DB', tablesScanned: 24, recordsScanned: 850000, piiFound: 14200, riskLevel: 'MEDIUM', status: 'COMPLETED', timestamp: '01-Aug-2026 11:30 PM' },
    { id: 's4', database: 'Legacy_Users', tablesScanned: 8, recordsScanned: 320000, piiFound: 8000, riskLevel: 'LOW', status: 'COMPLETED', timestamp: '01-Aug-2026 06:15 PM' },
  ];

  const list = scans && scans.length > 0 ? scans : defaultScans;

  const getRiskBadge = (risk) => {
    switch (risk) {
      case 'CRITICAL':
        return (
          <span className="px-2.5 py-1 rounded-md text-[10px] font-mono font-bold bg-rose-500/20 border border-rose-500/40 text-rose-400">
            CRITICAL
          </span>
        );
      case 'HIGH':
        return (
          <span className="px-2.5 py-1 rounded-md text-[10px] font-mono font-bold bg-orange-500/20 border border-orange-500/40 text-orange-400">
            HIGH
          </span>
        );
      case 'MEDIUM':
        return (
          <span className="px-2.5 py-1 rounded-md text-[10px] font-mono font-bold bg-amber-500/20 border border-amber-500/40 text-amber-400">
            MEDIUM
          </span>
        );
      case 'LOW':
        return (
          <span className="px-2.5 py-1 rounded-md text-[10px] font-mono font-bold bg-emerald-500/20 border border-emerald-500/40 text-emerald-400">
            LOW
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-1 rounded-md text-[10px] font-mono bg-slate-800 text-slate-400">
            {risk}
          </span>
        );
    }
  };

  return (
    <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Database className="w-4 h-4 text-emerald-400" />
          <h3 className="text-sm font-bold text-white uppercase tracking-wider">Recent PII Scan Results</h3>
        </div>
        <span className="text-[10px] font-mono text-slate-500">Database Audit Audit Log</span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs font-mono">
          <thead>
            <tr className="border-b border-slate-800 text-slate-400 text-[11px] uppercase tracking-wider">
              <th className="py-3 px-3">Database Target</th>
              <th className="py-3 px-3 text-center">Tables</th>
              <th className="py-3 px-3 text-right">Records Scanned</th>
              <th className="py-3 px-3 text-right">PII Detected</th>
              <th className="py-3 px-3 text-center">Risk Level</th>
              <th className="py-3 px-3 text-right">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {list.map((scan) => (
              <tr key={scan.id} className="hover:bg-slate-900/40 transition-colors">
                <td className="py-3.5 px-3 font-semibold text-white flex items-center gap-2">
                  <Database className="w-3.5 h-3.5 text-cyan-400" />
                  {scan.database}
                </td>
                <td className="py-3.5 px-3 text-center text-slate-300">{scan.tablesScanned}</td>
                <td className="py-3.5 px-3 text-right text-slate-300">{scan.recordsScanned?.toLocaleString()}</td>
                <td className="py-3.5 px-3 text-right font-bold text-cyan-300">{scan.piiFound?.toLocaleString()}</td>
                <td className="py-3.5 px-3 text-center">{getRiskBadge(scan.riskLevel)}</td>
                <td className="py-3.5 px-3 text-right text-slate-400 whitespace-nowrap">
                  {scan.status === 'COMPLETED' ? (
                    <span className="text-emerald-400 font-semibold">Completed</span>
                  ) : (
                    <span className="text-cyan-400 font-semibold flex items-center justify-end gap-1">
                      <RefreshCw className="w-3 h-3 animate-spin" /> Scanning
                    </span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
