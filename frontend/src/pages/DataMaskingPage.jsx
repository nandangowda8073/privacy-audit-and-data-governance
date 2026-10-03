import React from 'react';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck } from 'lucide-react';
import MaskingPolicyTable from '../components/masking/MaskingPolicyTable';
import MaskingPreviewPanel from '../components/masking/MaskingPreviewPanel';

const DataMaskingPage = () => {
  const { user } = useAuth();
  const role = user?.role || '';
  const canEdit = role === 'ROLE_SUPER_ADMIN' || role === 'ROLE_SECURITY_OFFICER';
  const canPreview = canEdit;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white tracking-tight flex items-center gap-3">
          <ShieldCheck className="w-8 h-8 text-cyan-500" />
          Dynamic Data Masking
        </h1>
        <p className="text-slate-400 mt-2">
          Configure and preview how sensitive PII data is dynamically masked based on user roles and risk levels.
          Original database values are <span className="text-cyan-400 font-semibold">never modified</span>.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Policy Table spans 2 cols */}
        <div className="lg:col-span-2">
          <MaskingPolicyTable canEdit={canEdit} />
        </div>

        {/* Preview Panel */}
        <div className="lg:col-span-1">
          {canPreview ? (
            <MaskingPreviewPanel />
          ) : (
            <div className="glass-panel p-5 rounded-2xl border border-slate-800 h-full flex items-center justify-center">
              <div className="text-center text-slate-500">
                <ShieldCheck className="w-10 h-10 mx-auto mb-2 opacity-30" />
                <p className="text-sm">Masking Preview is available to<br />Security Officers and Admins only.</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DataMaskingPage;
