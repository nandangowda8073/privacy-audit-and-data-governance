import React, { useState } from 'react';
import { Eye, Play } from 'lucide-react';
import { maskingApi } from '../../api/maskingApi';

const PII_TYPES = ['AADHAAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'BANK_ACCOUNT', 'CREDIT_CARD', 'DEBIT_CARD', 'IFSC', 'UPI_ID', 'EMAIL', 'PHONE', 'ADDRESS', 'PIN_CODE', 'PERSON_NAME', 'DATE_OF_BIRTH', 'CUSTOM'];

const EXAMPLES = {
  AADHAAR: '456789123456',
  PAN: 'ABCDE1234F',
  EMAIL: 'nandan@gmail.com',
  PHONE: '9876543210',
  CREDIT_CARD: '4532123412345678',
  BANK_ACCOUNT: '123456789012',
  IFSC: 'SBIN0001234',
  UPI_ID: 'nandan@upi',
  ADDRESS: '12 MG Road, Bengaluru, Karnataka',
  PERSON_NAME: 'Nandan Gowda',
  PASSPORT: 'A1234567',
  DRIVING_LICENSE: 'DL123456789012',
  DATE_OF_BIRTH: '01-01-1990',
  PIN_CODE: '560001',
  DEBIT_CARD: '4532123412345678',
};

const MASK_COLORS = {
  FULL:    'text-rose-400',
  PARTIAL: 'text-amber-400',
  NONE:    'text-emerald-400',
};

const RISK_COLORS = {
  CRITICAL: 'text-rose-400',
  HIGH:     'text-amber-400',
  MEDIUM:   'text-yellow-400',
  LOW:      'text-emerald-400',
};

const MaskingPreviewPanel = () => {
  const [piiType, setPiiType] = useState('AADHAAR');
  const [value, setValue] = useState(EXAMPLES['AADHAAR']);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleTypeChange = (type) => {
    setPiiType(type);
    setValue(EXAMPLES[type] || '');
    setResult(null);
  };

  const handlePreview = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await maskingApi.preview({ piiType, value });
      setResult(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Preview failed. Ensure you are logged in as SUPER_ADMIN or SECURITY_OFFICER.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-5">
      <div className="border-b border-slate-800 pb-4">
        <h2 className="text-lg font-bold text-white flex items-center gap-2">
          <Eye className="w-5 h-5 text-purple-400" />
          Masking Preview
        </h2>
        <p className="text-xs text-slate-400 mt-1">Test how a value is masked for your current role and the active policy.</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">PII Type</label>
          <select
            className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-slate-200 focus:border-purple-500 outline-none"
            value={piiType}
            onChange={e => handleTypeChange(e.target.value)}
          >
            {PII_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1">Value to Test</label>
          <input
            className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm text-slate-200 font-mono focus:border-purple-500 outline-none"
            value={value}
            onChange={e => setValue(e.target.value)}
            placeholder="Enter a sample value..."
          />
        </div>
      </div>

      <button
        onClick={handlePreview}
        disabled={loading || !value}
        className="w-full py-2.5 bg-purple-500/20 hover:bg-purple-500/30 border border-purple-500/40 rounded-lg text-purple-300 font-semibold text-sm transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
      >
        <Play className="w-4 h-4" />
        {loading ? 'Testing...' : 'Run Masking Preview'}
      </button>

      {error && (
        <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-lg text-rose-400 text-sm">
          {error}
        </div>
      )}

      {result && (
        <div className="p-4 bg-slate-950/60 border border-slate-700 rounded-xl space-y-3">
          <div className="flex justify-between text-sm">
            <span className="text-slate-400">Risk Level</span>
            <span className={`font-bold ${RISK_COLORS[result.riskLevel]}`}>{result.riskLevel}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-slate-400">Masking Type</span>
            <span className={`font-bold ${MASK_COLORS[result.maskingType]}`}>{result.maskingType}</span>
          </div>
          <div className="border-t border-slate-800 pt-3">
            <p className="text-xs text-slate-400 mb-1">Masked Value</p>
            <p className="font-mono text-lg text-white tracking-widest">{result.maskedValue}</p>
          </div>
          <div className="border-t border-slate-800 pt-3">
            <p className="text-xs text-slate-400 mb-1">Policy Reason</p>
            <p className="text-sm text-slate-300 italic">"{result.reason}"</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default MaskingPreviewPanel;
