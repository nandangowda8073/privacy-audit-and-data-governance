import React, { useState, useEffect } from 'react';
import { Settings, Save } from 'lucide-react';

const SchedulerConfigForm = ({ config, onSave, isSaving }) => {
  const [formData, setFormData] = useState({
    scanTime: '02:00',
    scanType: 'FULL',
    retryEnabled: true,
    retryCount: 3,
    retryDelay: 10,
    emailNotificationEnabled: false
  });

  useEffect(() => {
    if (config) {
      setFormData({
        scanTime: config.scanTime?.substring(0, 5) || '02:00',
        scanType: config.scanType || 'FULL',
        retryEnabled: config.retryEnabled || false,
        retryCount: config.retryCount || 3,
        retryDelay: config.retryDelay || 10,
        emailNotificationEnabled: config.emailNotificationEnabled || false
      });
    }
  }, [config]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800">
      <h2 className="text-xl font-semibold text-white mb-6 flex items-center gap-2">
        <Settings className="w-5 h-5 text-purple-400" />
        Schedule Configuration
      </h2>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-2">Daily Scan Time</label>
            <input
              type="time"
              name="scanTime"
              value={formData.scanTime}
              onChange={handleChange}
              className="w-full px-3 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white text-sm focus:border-cyan-500 outline-none"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-2">Scan Type</label>
            <select
              name="scanType"
              value={formData.scanType}
              onChange={handleChange}
              className="w-full px-3 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white text-sm focus:border-cyan-500 outline-none"
            >
              <option value="FULL">Full Scan</option>
              <option value="INCREMENTAL">Incremental Scan</option>
            </select>
          </div>
        </div>

        <div className="pt-4 border-t border-slate-800">
          <label className="flex items-center gap-3 cursor-pointer mb-4">
            <input
              type="checkbox"
              name="retryEnabled"
              checked={formData.retryEnabled}
              onChange={handleChange}
              className="w-4 h-4 rounded border-slate-700 bg-slate-900 text-cyan-500 focus:ring-cyan-500"
            />
            <span className="text-sm font-medium text-white">Enable Auto-Retry on Failure</span>
          </label>
          
          {formData.retryEnabled && (
            <div className="grid grid-cols-2 gap-4 pl-7">
              <div>
                <label className="block text-xs text-slate-400 mb-1">Max Retry Attempts</label>
                <input
                  type="number"
                  name="retryCount"
                  min="1" max="10"
                  value={formData.retryCount}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-lg bg-slate-900/50 border border-slate-700 text-white text-sm focus:border-cyan-500 outline-none"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-400 mb-1">Retry Delay (Minutes)</label>
                <input
                  type="number"
                  name="retryDelay"
                  min="1" max="60"
                  value={formData.retryDelay}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-lg bg-slate-900/50 border border-slate-700 text-white text-sm focus:border-cyan-500 outline-none"
                />
              </div>
            </div>
          )}
        </div>

        <div className="pt-4 border-t border-slate-800">
          <label className="flex items-center gap-3 cursor-pointer">
            <input
              type="checkbox"
              name="emailNotificationEnabled"
              checked={formData.emailNotificationEnabled}
              onChange={handleChange}
              className="w-4 h-4 rounded border-slate-700 bg-slate-900 text-cyan-500 focus:ring-cyan-500"
            />
            <span className="text-sm font-medium text-white">Send Email Notification on Completion</span>
          </label>
        </div>

        <button
          type="submit"
          disabled={isSaving}
          className="w-full mt-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2 border border-slate-700"
        >
          {isSaving ? (
             <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <Save className="w-4 h-4" />
          )}
          Save Configuration
        </button>
      </form>
    </div>
  );
};

export default SchedulerConfigForm;
