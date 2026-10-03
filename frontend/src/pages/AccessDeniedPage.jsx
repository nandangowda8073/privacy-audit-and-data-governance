import React from 'react';
import { ShieldOff, ArrowLeft } from 'lucide-react';

const AccessDeniedPage = ({ onBack }) => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] gap-6 text-center px-4">
      <div className="relative">
        <div className="w-24 h-24 rounded-full bg-rose-500/10 border border-rose-500/30 flex items-center justify-center animate-pulse">
          <ShieldOff className="w-12 h-12 text-rose-400" />
        </div>
        <div className="absolute -top-1 -right-1 w-5 h-5 rounded-full bg-rose-500 border-2 border-[#0a0d14] flex items-center justify-center text-[10px] font-bold text-white">
          !
        </div>
      </div>
      
      <div className="space-y-2">
        <h1 className="text-3xl font-bold text-rose-400 font-mono">403</h1>
        <h2 className="text-xl font-semibold text-slate-100">Access Denied</h2>
        <p className="text-slate-400 text-sm max-w-md">
          You do not have the required permissions to view this page. 
          Contact your system administrator if you believe this is an error.
        </p>
      </div>

      <div className="flex gap-3">
        <button
          onClick={onBack}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-800 border border-slate-700 text-slate-300 hover:text-white hover:border-slate-600 transition-all text-sm font-medium"
        >
          <ArrowLeft className="w-4 h-4" />
          Go Back
        </button>
      </div>
    </div>
  );
};

export default AccessDeniedPage;
