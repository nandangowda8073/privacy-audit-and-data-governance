import React, { useState } from 'react';
import { Plus, Edit3, Trash2, X, Save, ToggleLeft, ToggleRight } from 'lucide-react';
import { piiApi } from '../../api/piiApi';

const CATEGORIES = [
  'PERSONAL_IDENTITY', 'FINANCIAL', 'CONTACT', 'PERSONAL_INFORMATION', 'CUSTOM'
];

const friendlyLabel = (key) =>
  (key || '').replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());

const emptyForm = {
  patternName: '', category: 'CUSTOM', regex: '', description: '', enabled: true
};

const CustomPatternManager = ({ patterns, onRefresh }) => {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editing, setEditing]       = useState(null);
  const [form, setForm]             = useState(emptyForm);
  const [saving, setSaving]         = useState(false);
  const [error, setError]           = useState('');

  const openAdd = () => { setEditing(null); setForm(emptyForm); setError(''); setIsFormOpen(true); };
  const openEdit = (p) => {
    setEditing(p);
    setForm({ patternName: p.patternName, category: p.category, regex: p.regex, description: p.description, enabled: p.enabled });
    setError('');
    setIsFormOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editing) await piiApi.updatePattern(editing.id, form);
      else         await piiApi.createPattern(form);
      setIsFormOpen(false);
      onRefresh();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save pattern.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this custom pattern?')) return;
    try {
      await piiApi.deletePattern(id);
      onRefresh();
    } catch (err) {
      alert(err.response?.data?.message || 'Delete failed.');
    }
  };

  return (
    <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden">
      <div className="p-4 border-b border-slate-800 flex items-center justify-between">
        <h3 className="text-sm font-semibold text-white">Custom Detection Patterns</h3>
        <button
          onClick={openAdd}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl text-xs font-semibold transition-all"
        >
          <Plus className="w-3.5 h-3.5" /> Add Pattern
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-slate-800 bg-slate-950/60 text-slate-400 font-mono text-[11px] uppercase">
              <th className="py-3 px-4">Name</th>
              <th className="py-3 px-4">Category</th>
              <th className="py-3 px-4">Regex</th>
              <th className="py-3 px-4">Description</th>
              <th className="py-3 px-4">Status</th>
              <th className="py-3 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/50">
            {(patterns || []).length === 0 ? (
              <tr>
                <td colSpan="6" className="py-10 text-center text-slate-500">
                  No custom patterns. Add one to extend the detection engine.
                </td>
              </tr>
            ) : (
              (patterns || []).map((p) => (
                <tr key={p.id} className="hover:bg-slate-800/20 transition-colors">
                  <td className="py-3 px-4 font-semibold text-white">{p.patternName}</td>
                  <td className="py-3 px-4 text-purple-400 font-mono">{friendlyLabel(p.category)}</td>
                  <td className="py-3 px-4 font-mono text-amber-300 max-w-[200px] truncate" title={p.regex}>{p.regex}</td>
                  <td className="py-3 px-4 text-slate-400 max-w-[200px] truncate" title={p.description}>{p.description || '—'}</td>
                  <td className="py-3 px-4">
                    {p.enabled ? (
                      <span className="text-emerald-400 flex items-center gap-1">
                        <ToggleRight className="w-4 h-4" /> Active
                      </span>
                    ) : (
                      <span className="text-slate-500 flex items-center gap-1">
                        <ToggleLeft className="w-4 h-4" /> Disabled
                      </span>
                    )}
                  </td>
                  <td className="py-3 px-4 text-right">
                    <div className="flex items-center justify-end gap-1.5">
                      <button onClick={() => openEdit(p)} className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-blue-400 transition-all">
                        <Edit3 className="w-3.5 h-3.5" />
                      </button>
                      <button onClick={() => handleDelete(p.id)} className="p-1.5 rounded-lg bg-slate-800 hover:bg-rose-900/40 text-rose-400 transition-all">
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {isFormOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="w-full max-w-lg glass-panel rounded-2xl border border-slate-700 bg-[#0c1019] shadow-2xl p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-white text-base">
                {editing ? 'Edit Pattern' : 'Add Custom Pattern'}
              </h3>
              <button onClick={() => setIsFormOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {error && (
              <div className="p-3 bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs rounded-xl">{error}</div>
            )}

            <form onSubmit={handleSave} className="space-y-3">
              <div>
                <label className="text-xs text-slate-300 block mb-1">Pattern Name *</label>
                <input required value={form.patternName} onChange={(e) => setForm({ ...form, patternName: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white focus:border-cyan-500 outline-none" />
              </div>
              <div>
                <label className="text-xs text-slate-300 block mb-1">Category *</label>
                <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white focus:border-cyan-500 outline-none">
                  {CATEGORIES.map((c) => <option key={c} value={c}>{friendlyLabel(c)}</option>)}
                </select>
              </div>
              <div>
                <label className="text-xs text-slate-300 block mb-1">Regex Pattern *</label>
                <input required value={form.regex} onChange={(e) => setForm({ ...form, regex: e.target.value })}
                  placeholder="e.g. EMP-\d{6}"
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white font-mono focus:border-cyan-500 outline-none" />
              </div>
              <div>
                <label className="text-xs text-slate-300 block mb-1">Description</label>
                <input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white focus:border-cyan-500 outline-none" />
              </div>
              <label className="flex items-center gap-3 cursor-pointer pt-1">
                <input type="checkbox" checked={form.enabled} onChange={(e) => setForm({ ...form, enabled: e.target.checked })}
                  className="w-4 h-4 rounded border-slate-700 bg-slate-900 text-cyan-500" />
                <span className="text-sm text-white">Enable Pattern</span>
              </label>
              <button type="submit" disabled={saving}
                className="w-full py-2.5 bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50">
                {saving ? <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <Save className="w-4 h-4" />}
                {saving ? 'Saving...' : 'Save Pattern'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomPatternManager;
