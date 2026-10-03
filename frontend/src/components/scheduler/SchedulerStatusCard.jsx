import React from 'react';
import { Play, Square, Activity, Clock, Database, AlertTriangle } from 'lucide-react';

const SchedulerStatusCard = ({ status, onStart, onStop, onTriggerScan, isTriggering }) => {
  const isEnabled = status?.enabled;
  const isRunning = status?.currentStatus === 'RUNNING';

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-semibold text-white flex items-center gap-2">
            <Activity className="w-5 h-5 text-cyan-400" />
            Scheduler Engine
          </h2>
          <p className="text-sm text-slate-400 mt-1">Manage automated scan execution</p>
        </div>
        <div className={`px-3 py-1 rounded-full text-xs font-semibold border ${isEnabled ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-slate-800 text-slate-400 border-slate-700'}`}>
          {isEnabled ? '● Active' : '○ Disabled'}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center gap-1">
            <Clock className="w-3.5 h-3.5" /> Next Scheduled Scan
          </div>
          <div className="text-lg font-semibold text-white">
            {isEnabled && status?.nextScanTime ? status.nextScanTime : 'Not scheduled'}
          </div>
        </div>
        <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-800">
          <div className="text-slate-400 text-xs mb-1 flex items-center gap-1">
            <Database className="w-3.5 h-3.5" /> Last Scan Status
          </div>
          <div className="text-lg font-semibold text-white">
            {status?.currentStatus || 'Unknown'}
          </div>
        </div>
      </div>

      <div className="flex flex-wrap gap-3">
        {isEnabled ? (
          <button onClick={onStop} className="flex-1 py-2.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2">
            <Square className="w-4 h-4 fill-current" /> Stop Scheduler
          </button>
        ) : (
          <button onClick={onStart} className="flex-1 py-2.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2">
            <Play className="w-4 h-4 fill-current" /> Enable Scheduler
          </button>
        )}
        <button 
          onClick={onTriggerScan} 
          disabled={isRunning || isTriggering}
          className="flex-1 py-2.5 bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50"
        >
          {isTriggering || isRunning ? (
            <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <AlertTriangle className="w-4 h-4" />
          )}
          {isRunning ? 'Scan in Progress...' : 'Trigger Manual Scan'}
        </button>
      </div>
    </div>
  );
};

export default SchedulerStatusCard;
