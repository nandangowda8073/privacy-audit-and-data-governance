import React, { useEffect, useState } from 'react';
import { Users, Search, Shield, UserCheck, UserX, RefreshCw, Loader2, ChevronDown, XCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import API from '../api/axios';

const ROLES = ['ROLE_SUPER_ADMIN', 'ROLE_SECURITY_OFFICER', 'ROLE_AUDITOR', 'ROLE_VIEWER'];

const ROLE_COLORS = {
  ROLE_SUPER_ADMIN: 'text-violet-400 bg-violet-500/10 border-violet-500/30',
  ROLE_SECURITY_OFFICER: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30',
  ROLE_AUDITOR: 'text-amber-400 bg-amber-500/10 border-amber-500/30',
  ROLE_VIEWER: 'text-slate-400 bg-slate-500/10 border-slate-500/30',
};

const UserManagementPage = () => {
  const { hasPermission } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);
  const [processingId, setProcessingId] = useState(null);

  const canEdit = hasPermission('USER_UPDATE');
  const canDelete = hasPermission('USER_DELETE');

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await API.get('/v1/users');
      if (res.data?.success) setUsers(res.data.data || []);
    } catch {
      setError('Failed to load users. You may not have access.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const handleRoleChange = async (userId, newRole, username) => {
    setProcessingId(userId);
    try {
      await API.put(`/v1/users/${userId}/role?role=${newRole}`);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: newRole } : u));
      showToast(`Role updated for ${username}`);
    } catch {
      showToast('Failed to update role.', 'error');
    } finally {
      setProcessingId(null);
    }
  };

  const handleStatusChange = async (userId, active, username) => {
    setProcessingId(userId);
    try {
      await API.put(`/v1/users/${userId}/status?active=${active}`);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, active } : u));
      showToast(`${username} ${active ? 'enabled' : 'disabled'}`);
    } catch {
      showToast('Failed to update status.', 'error');
    } finally {
      setProcessingId(null);
    }
  };

  const handleDelete = async (userId, username) => {
    if (!window.confirm(`Delete user "${username}"? This cannot be undone.`)) return;
    setProcessingId(userId);
    try {
      await API.delete(`/v1/users/${userId}`);
      setUsers(prev => prev.filter(u => u.id !== userId));
      showToast(`${username} deleted`);
    } catch {
      showToast('Failed to delete user.', 'error');
    } finally {
      setProcessingId(null);
    }
  };

  const filtered = users.filter(u =>
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase()) ||
    u.fullName?.toLowerCase().includes(search.toLowerCase())
  );

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
        <button onClick={fetchUsers} className="text-cyan-400 hover:text-cyan-300 text-sm flex items-center gap-1 mx-auto">
          <RefreshCw className="w-3.5 h-3.5" /> Retry
        </button>
      </div>
    </div>
  );

  return (
    <div className="p-6 space-y-6">
      {toast && (
        <div className={`fixed top-4 right-4 z-50 px-4 py-2.5 rounded-xl border text-sm font-medium shadow-xl transition-all
          ${toast.type === 'success' ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-300' : 'bg-rose-500/20 border-rose-500/40 text-rose-300'}`}>
          {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center">
            <Users className="w-5 h-5 text-cyan-400" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-100">User Management</h1>
            <p className="text-slate-500 text-xs mt-0.5">{users.length} total users</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
            <input
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search users..."
              className="pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500/50 w-64"
            />
          </div>
          <button onClick={fetchUsers} className="p-2 rounded-xl border border-slate-800 text-slate-400 hover:text-slate-200 hover:border-slate-700 transition-all">
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-900/80">
                <th className="text-left px-4 py-3 text-xs font-mono text-slate-500 uppercase tracking-widest">User</th>
                <th className="text-left px-4 py-3 text-xs font-mono text-slate-500 uppercase tracking-widest">Role</th>
                <th className="text-left px-4 py-3 text-xs font-mono text-slate-500 uppercase tracking-widest">Status</th>
                <th className="text-left px-4 py-3 text-xs font-mono text-slate-500 uppercase tracking-widest">Joined</th>
                {(canEdit || canDelete) && <th className="text-right px-4 py-3 text-xs font-mono text-slate-500 uppercase tracking-widest">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/50">
              {filtered.map(user => {
                const colorClass = ROLE_COLORS[user.role] || 'text-slate-400 bg-slate-500/10 border-slate-500/30';
                const roleName = user.role?.replace('ROLE_', '').replace(/_/g, ' ') || '-';
                const isProcessing = processingId === user.id;
                return (
                  <tr key={user.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-cyan-500/20 to-blue-500/20 border border-cyan-500/30 flex items-center justify-center text-cyan-400 font-bold text-xs">
                          {user.fullName?.charAt(0) || user.username?.charAt(0) || '?'}
                        </div>
                        <div>
                          <p className="text-slate-100 font-medium">{user.fullName || user.username}</p>
                          <p className="text-slate-500 text-xs">{user.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      {canEdit ? (
                        <div className="relative inline-block">
                          <select
                            value={user.role || ''}
                            onChange={e => handleRoleChange(user.id, e.target.value, user.username)}
                            disabled={isProcessing}
                            className={`text-xs font-mono pl-2.5 pr-7 py-1.5 rounded-lg border appearance-none cursor-pointer transition-all bg-transparent ${colorClass}`}
                          >
                            {ROLES.map(r => (
                              <option key={r} value={r} className="bg-slate-900 text-slate-200">
                                {r.replace('ROLE_', '').replace(/_/g, ' ')}
                              </option>
                            ))}
                          </select>
                          <ChevronDown className="absolute right-2 top-1/2 -translate-y-1/2 w-3 h-3 pointer-events-none text-current" />
                        </div>
                      ) : (
                        <span className={`text-xs font-mono px-2 py-1 rounded-lg border ${colorClass}`}>{roleName}</span>
                      )}
                    </td>
                    <td className="px-4 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full border
                        ${user.active ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' : 'text-rose-400 bg-rose-500/10 border-rose-500/30'}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${user.active ? 'bg-emerald-400 animate-pulse' : 'bg-rose-400'}`} />
                        {user.active ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 text-slate-500 text-xs font-mono">
                      {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-'}
                    </td>
                    {(canEdit || canDelete) && (
                      <td className="px-4 py-3.5">
                        <div className="flex items-center justify-end gap-2">
                          {isProcessing ? (
                            <Loader2 className="w-4 h-4 animate-spin text-cyan-400" />
                          ) : (
                            <>
                              {canEdit && (
                                <button
                                  onClick={() => handleStatusChange(user.id, !user.active, user.username)}
                                  title={user.active ? 'Disable user' : 'Enable user'}
                                  className={`p-1.5 rounded-lg border transition-all ${user.active
                                    ? 'text-amber-400 border-amber-500/30 bg-amber-500/10 hover:bg-amber-500/20'
                                    : 'text-emerald-400 border-emerald-500/30 bg-emerald-500/10 hover:bg-emerald-500/20'}`}
                                >
                                  {user.active ? <UserX className="w-3.5 h-3.5" /> : <UserCheck className="w-3.5 h-3.5" />}
                                </button>
                              )}
                              {canDelete && (
                                <button
                                  onClick={() => handleDelete(user.id, user.username)}
                                  title="Delete user"
                                  className="p-1.5 rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 transition-all"
                                >
                                  <XCircle className="w-3.5 h-3.5" />
                                </button>
                              )}
                            </>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
          {filtered.length === 0 && (
            <div className="text-center py-12 text-slate-500 text-sm">No users found.</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserManagementPage;
