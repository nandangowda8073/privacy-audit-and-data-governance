import React, { useEffect, useState } from 'react';
import { KeyRound, Shield, CheckCircle, XCircle, Loader2, RefreshCw, ChevronDown } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import API from '../api/axios';

const ROLE_COLORS = {
  ROLE_SUPER_ADMIN: 'text-violet-400 bg-violet-500/10 border-violet-500/30',
  ROLE_SECURITY_OFFICER: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30',
  ROLE_AUDITOR: 'text-amber-400 bg-amber-500/10 border-amber-500/30',
  ROLE_VIEWER: 'text-slate-400 bg-slate-500/10 border-slate-500/30',
};

const PERMISSION_GROUPS = {
  'User Management': ['USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE'],
  'Role Management': ['ROLE_VIEW', 'ROLE_MANAGE'],
  'Database': ['DATABASE_VIEW', 'DATABASE_CREATE', 'DATABASE_UPDATE', 'DATABASE_DELETE', 'DATABASE_TEST_CONNECTION'],
  'Scanning': ['SCAN_VIEW', 'SCAN_START', 'SCAN_CANCEL', 'SCAN_SCHEDULE'],
  'PII': ['PII_VIEW', 'PII_EXPORT'],
  'Risk': ['RISK_VIEW'],
  'Masking': ['MASKING_POLICY_VIEW', 'MASKING_POLICY_CREATE', 'MASKING_POLICY_UPDATE', 'MASKING_POLICY_DELETE'],
  'Audit & Reports': ['AUDIT_LOG_VIEW', 'AUDIT_LOG_EXPORT', 'REPORT_VIEW', 'REPORT_GENERATE', 'REPORT_DOWNLOAD'],
  'General': ['DASHBOARD_VIEW', 'NOTIFICATION_VIEW'],
};

const RolesPermissionsPage = () => {
  const { hasPermission } = useAuth();
  const [matrix, setMatrix] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(null);
  const [error, setError] = useState(null);
  const [editMode, setEditMode] = useState(null);
  const [editPermissions, setEditPermissions] = useState([]);
  const [expandedGroup, setExpandedGroup] = useState(null);
  const [toast, setToast] = useState(null);

  const canManage = hasPermission('ROLE_MANAGE');

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchMatrix = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await API.get('/roles/permissions');
      if (res.data?.success) setMatrix(res.data.data || {});
    } catch {
      setError('Failed to load role permissions. You may not have access.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchMatrix(); }, []);

  const startEdit = (role) => {
    setEditMode(role);
    setEditPermissions([...(matrix[role] || [])]);
  };

  const cancelEdit = () => { setEditMode(null); setEditPermissions([]); };

  const togglePermission = (permission) => {
    setEditPermissions(prev =>
      prev.includes(permission) ? prev.filter(p => p !== permission) : [...prev, permission]
    );
  };

  const savePermissions = async (role) => {
    setSaving(role);
    try {
      await API.put(`/roles/${role}/permissions`, editPermissions);
      setMatrix(prev => ({ ...prev, [role]: editPermissions }));
      setEditMode(null);
      setEditPermissions([]);
      showToast(`Permissions updated for ${role.replace('ROLE_', '').replace('_', ' ')}`);
    } catch {
      showToast('Failed to save permissions.', 'error');
    } finally {
      setSaving(null);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <Loader2 className="w-8 h-8 text-cyan-400 animate-spin" />
    </div>
  );

  if (error) return (
    <div className="flex items-center justify-center h-64">
      <div className="text-center space-y-2">
        <XCircle className="w-10 h-10 text-rose-400 mx-auto" />
        <p className="text-slate-400 text-sm">{error}</p>
        <button onClick={fetchMatrix} className="text-cyan-400 hover:text-cyan-300 text-sm flex items-center gap-1 mx-auto">
          <RefreshCw className="w-3.5 h-3.5" /> Retry
        </button>
      </div>
    </div>
  );

  const roles = Object.keys(matrix);

  return (
    <div className="p-6 space-y-6">
      {/* Toast */}
      {toast && (
        <div className={`fixed top-4 right-4 z-50 px-4 py-2.5 rounded-xl border text-sm font-medium shadow-xl transition-all
          ${toast.type === 'success' ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-300' : 'bg-rose-500/20 border-rose-500/40 text-rose-300'}`}>
          {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-violet-500/10 border border-violet-500/30 flex items-center justify-center">
            <KeyRound className="w-5 h-5 text-violet-400" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-100">Roles & Permissions</h1>
            <p className="text-slate-500 text-xs mt-0.5">Manage role-based access control matrix</p>
          </div>
        </div>
        <button onClick={fetchMatrix} className="text-slate-400 hover:text-slate-200 text-sm flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-800 hover:border-slate-700 transition-all">
          <RefreshCw className="w-3.5 h-3.5" /> Refresh
        </button>
      </div>

      {/* Role Cards */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
        {roles.map(role => {
          const permissions = editMode === role ? editPermissions : (matrix[role] || []);
          const isEditing = editMode === role;
          const colorClass = ROLE_COLORS[role] || 'text-slate-400 bg-slate-500/10 border-slate-500/30';
          const roleName = role.replace('ROLE_', '').replace(/_/g, ' ');

          return (
            <div key={role} className="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden">
              {/* Card Header */}
              <div className="p-4 border-b border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Shield className={`w-5 h-5 ${colorClass.split(' ')[0]}`} />
                  <div>
                    <span className={`text-xs font-mono font-semibold px-2.5 py-1 rounded-lg border ${colorClass}`}>{roleName}</span>
                    <span className="text-slate-500 text-xs ml-2">{permissions.length} permissions</span>
                  </div>
                </div>
                {canManage && (
                  <div className="flex gap-2">
                    {isEditing ? (
                      <>
                        <button
                          onClick={() => savePermissions(role)}
                          disabled={saving === role}
                          className="px-3 py-1.5 rounded-lg bg-emerald-500/20 border border-emerald-500/40 text-emerald-300 text-xs hover:bg-emerald-500/30 transition-all flex items-center gap-1.5"
                        >
                          {saving === role ? <Loader2 className="w-3 h-3 animate-spin" /> : <CheckCircle className="w-3 h-3" />}
                          Save
                        </button>
                        <button onClick={cancelEdit} className="px-3 py-1.5 rounded-lg bg-slate-800 border border-slate-700 text-slate-400 text-xs hover:text-slate-200 transition-all">
                          Cancel
                        </button>
                      </>
                    ) : (
                      <button onClick={() => startEdit(role)} className="px-3 py-1.5 rounded-lg bg-slate-800 border border-slate-700 text-slate-400 text-xs hover:text-slate-200 hover:border-slate-600 transition-all">
                        Edit
                      </button>
                    )}
                  </div>
                )}
              </div>

              {/* Permission Groups */}
              <div className="p-4 space-y-3">
                {Object.entries(PERMISSION_GROUPS).map(([group, perms]) => {
                  const groupKey = `${role}-${group}`;
                  const isExpanded = expandedGroup === groupKey;
                  const activeCount = perms.filter(p => permissions.includes(p)).length;
                  return (
                    <div key={group} className="border border-slate-800 rounded-xl overflow-hidden">
                      <button
                        onClick={() => setExpandedGroup(isExpanded ? null : groupKey)}
                        className="w-full flex items-center justify-between p-2.5 text-xs font-medium text-slate-300 hover:bg-slate-800/50 transition-all"
                      >
                        <span>{group}</span>
                        <div className="flex items-center gap-2">
                          <span className={`font-mono text-[11px] px-1.5 py-0.5 rounded ${activeCount === perms.length ? 'text-emerald-400 bg-emerald-500/10' : activeCount === 0 ? 'text-slate-600 bg-slate-800' : 'text-amber-400 bg-amber-500/10'}`}>
                            {activeCount}/{perms.length}
                          </span>
                          <ChevronDown className={`w-3.5 h-3.5 text-slate-500 transition-transform ${isExpanded ? 'rotate-180' : ''}`} />
                        </div>
                      </button>
                      {isExpanded && (
                        <div className="px-3 pb-3 grid grid-cols-1 sm:grid-cols-2 gap-1.5">
                          {perms.map(p => {
                            const granted = permissions.includes(p);
                            return (
                              <div
                                key={p}
                                onClick={() => isEditing && togglePermission(p)}
                                className={`flex items-center gap-2 px-2.5 py-1.5 rounded-lg text-[11px] font-mono transition-all
                                  ${isEditing ? 'cursor-pointer hover:bg-slate-800/80' : ''}
                                  ${granted ? 'text-emerald-300 bg-emerald-500/10 border border-emerald-500/20' : 'text-slate-600 bg-slate-900 border border-slate-800'}`}
                              >
                                {granted ? <CheckCircle className="w-3 h-3 text-emerald-400 shrink-0" /> : <XCircle className="w-3 h-3 text-slate-700 shrink-0" />}
                                {p}
                              </div>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default RolesPermissionsPage;
