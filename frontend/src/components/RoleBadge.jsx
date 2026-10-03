import React from 'react';
import { ShieldCheck, ShieldAlert, FileSearch } from 'lucide-react';

export const RoleBadge = ({ role }) => {
  switch (role) {
    case 'ROLE_SUPER_ADMIN':
      return (
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-rose-500/10 border border-rose-500/30 text-rose-400">
          <ShieldAlert className="w-3.5 h-3.5" />
          Super Admin
        </span>
      );
    case 'ROLE_SECURITY_OFFICER':
      return (
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-cyan-500/10 border border-cyan-500/30 text-cyan-400">
          <ShieldCheck className="w-3.5 h-3.5" />
          Security Officer
        </span>
      );
    case 'ROLE_AUDITOR':
      return (
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-purple-500/10 border border-purple-500/30 text-purple-400">
          <FileSearch className="w-3.5 h-3.5" />
          Compliance Auditor
        </span>
      );
    default:
      return (
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-slate-500/10 border border-slate-500/30 text-slate-400">
          {role || 'User'}
        </span>
      );
  }
};
