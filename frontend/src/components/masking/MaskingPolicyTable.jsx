import React, { useState, useEffect } from 'react';
import { Plus, Edit2, Trash2, ToggleLeft, ToggleRight, Shield } from 'lucide-react';
import { maskingApi } from '../../api/maskingApi';

const RISK_COLORS = {
  CRITICAL: 'text-rose-400 bg-rose-500/10 border-rose-500/30',
  HIGH:     'text-amber-400 bg-amber-500/10 border-amber-500/30',
  MEDIUM:   'text-yellow-400 bg-yellow-500/10 border-yellow-500/30',
  LOW:      'text-emerald-400 bg-emerald-500/10 border-emerald-500/30',
};
const MASK_COLORS = {
  FULL:    'text-rose-400 bg-rose-500/10 border-rose-500/30',
  PARTIAL: 'text-amber-400 bg-amber-500/10 border-amber-500/30',
  NONE:    'text-emerald-400 bg-emerald-500/10 border-emerald-500/30',
};

const ROLES = ['ROLE_SUPER_ADMIN', 'ROLE_SECURITY_OFFICER', 'ROLE_AUDITOR', 'ROLE_VIEWER'];
const PII_TYPES = ['AADHAAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'BANK_ACCOUNT', 'CREDIT_CARD', 'DEBIT_CARD', 'IFSC', 'UPI_ID', 'EMAIL', 'PHONE', 'ADDRESS', 'PIN_CODE', 'PERSON_NAME', 'DATE_OF_BIRTH', 'CITY', 'COUNTRY', 'CUSTOM'];
const RISK_LEVELS = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const MASKING_TYPES = ['NONE', 'PARTIAL', 'FULL'];

const emptyForm = { role: 'ROLE_VIEWER', piiType: 'AADHAAR', riskLevel: 'CRITICAL', maskingType: 'FULL', description: '', enabled: true };

const MaskingPolicyTable = ({ canEdit }) => {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const fetchPolicies = async () => {
    try {
      const res = await maskingApi.getPolicies();
      setPolicies(res.data.data || []);
    } catch (err) {
      console.error('Failed to fetch policies:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPolicies(); }, []);

  const openCreate = () => { setEditingId(null); setForm(emptyForm); setShowModal(true); };
  const openEdit = (p) => { setEditingId(p.id); setForm({ role: p.role, piiType: p.piiType, riskLevel: p.riskLevel, maskingType: p.maskingType, description: p.description || '', enabled: p.enabled }); setShowModal(true); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (editingId) {
        await maskingApi.updatePolicy(editingId, form);
      } else {
        await maskingApi.createPolicy(form);
      }
      setShowModal(false);
      fetchPolicies();
    } catch (err) {
      console.error('Save failed:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this policy?')) return;
    try {
      await maskingApi.deletePolicy(id);
      fetchPolicies();
    } catch (err) {
      console.error('Delete failed:', err);
    }
  };

  const handleToggle = async (p) => {
    try {
      await maskingApi.updatePolicy(p.id, { ...p, enabled: !p.enabled });
      fetchPolicies();
    } catch (err) {
      console.error('Toggle failed:', err);
    }
  };

  const selectClass = "w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-slate-200 focus:border-cyan-500 outline-none";
  const inputClass = "w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-slate-200 focus:border-cyan-500 outline-none";

  if (loading) return <div className="h-40 flex items-center justify-center"><div className="w-6 h-6 border-4 border-cyan-500/30 border-t-cyan-500 rounded-full animate-spin" /></div>;

  return (
    <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex justify-between items-center border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Shield className="w-5 h-5 text-cyan-400" />
            Masking Policies
          </h2>
          <p className="text-xs text-slate-400 mt-1">Configure role-based data masking rules.</p>
        </div>
        {canEdit && (
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-lg text-cyan-400 text-sm transition-colors">
            <Plus className="w-4 h-4" /> Add Policy
          </button>
        )}
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
              <th className="p-3">Role</th>
              <th className="p-3">PII Type</th>
              <th className="p-3">Risk Level</th>
              <th className="p-3">Masking</th>
              <th className="p-3">Status</th>
              <th className="p-3">Description</th>
              {canEdit && <th className="p-3">Actions</th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/50">
            {policies.length > 0 ? policies.map(p => (
              <tr key={p.id} className={`hover:bg-slate-800/30 transition-colors ${!p.enabled ? 'opacity-50' : ''}`}>
                <td className="p-3 text-xs text-slate-300 font-mono">{p.role?.replace('ROLE_', '')}</td>
                <td className="p-3 text-xs font-mono text-slate-200">{p.piiType}</td>
                <td className="p-3">
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${RISK_COLORS[p.riskLevel] || ''}`}>{p.riskLevel}</span>
                </td>
                <td className="p-3">
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${MASK_COLORS[p.maskingType] || ''}`}>{p.maskingType}</span>
                </td>
                <td className="p-3">
                  {canEdit ? (
                    <button onClick={() => handleToggle(p)} className="text-slate-400 hover:text-cyan-400 transition-colors">
                      {p.enabled ? <ToggleRight className="w-5 h-5 text-emerald-400" /> : <ToggleLeft className="w-5 h-5" />}
                    </button>
                  ) : (
                    <span className={`text-xs font-semibold ${p.enabled ? 'text-emerald-400' : 'text-slate-500'}`}>{p.enabled ? 'Active' : 'Disabled'}</span>
                  )}
                </td>
                <td className="p-3 text-xs text-slate-500 max-w-xs truncate">{p.description || '-'}</td>
                {canEdit && (
                  <td className="p-3">
                    <div className="flex items-center gap-2">
                      <button onClick={() => openEdit(p)} className="text-slate-400 hover:text-cyan-400 transition-colors"><Edit2 className="w-4 h-4" /></button>
                      <button onClick={() => handleDelete(p.id)} className="text-slate-400 hover:text-rose-400 transition-colors"><Trash2 className="w-4 h-4" /></button>
                    </div>
                  </td>
                )}
              </tr>
            )) : (
              <tr><td colSpan={canEdit ? 7 : 6} className="p-8 text-center text-slate-500 text-sm">No masking policies defined. Click "Add Policy" to create one.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl p-6 w-full max-w-md shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-5">{editingId ? 'Edit Policy' : 'New Masking Policy'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Role</label>
                <select className={selectClass} value={form.role} onChange={e => setForm(f => ({...f, role: e.target.value}))}>
                  {ROLES.map(r => <option key={r} value={r}>{r.replace('ROLE_', '')}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">PII Type</label>
                <select className={selectClass} value={form.piiType} onChange={e => setForm(f => ({...f, piiType: e.target.value}))}>
                  {PII_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Risk Level</label>
                <select className={selectClass} value={form.riskLevel} onChange={e => setForm(f => ({...f, riskLevel: e.target.value}))}>
                  {RISK_LEVELS.map(r => <option key={r} value={r}>{r}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Masking Type</label>
                <select className={selectClass} value={form.maskingType} onChange={e => setForm(f => ({...f, maskingType: e.target.value}))}>
                  {MASKING_TYPES.map(m => <option key={m} value={m}>{m}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Description</label>
                <input className={inputClass} placeholder="Optional description..." value={form.description} onChange={e => setForm(f => ({...f, description: e.target.value}))} />
              </div>
              <div className="flex items-center gap-2">
                <input type="checkbox" id="enabled" checked={form.enabled} onChange={e => setForm(f => ({...f, enabled: e.target.checked}))} className="accent-cyan-500" />
                <label htmlFor="enabled" className="text-sm text-slate-300">Enabled</label>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="submit" disabled={saving} className="flex-1 py-2 bg-cyan-500 hover:bg-cyan-400 rounded-lg text-slate-900 font-bold text-sm transition-colors disabled:opacity-50">
                  {saving ? 'Saving...' : editingId ? 'Update Policy' : 'Create Policy'}
                </button>
                <button type="button" onClick={() => setShowModal(false)} className="flex-1 py-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-300 text-sm transition-colors">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default MaskingPolicyTable;
