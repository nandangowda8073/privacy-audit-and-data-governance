import React, { useState } from 'react';
import { ScanLine, Upload, FileText, X, Play } from 'lucide-react';
import { piiApi } from '../../api/piiApi';

const DetectionPanel = ({ onResult }) => {
  const [mode, setMode]     = useState('text'); // 'text' | 'file'
  const [text, setText]     = useState('');
  const [file, setFile]     = useState(null);
  const [scanning, setScanning] = useState(false);
  const [error, setError]   = useState('');

  const handleScan = async () => {
    setError('');
    setScanning(true);
    try {
      let res;
      if (mode === 'text') {
        if (!text.trim()) { setError('Please enter some text to scan.'); setScanning(false); return; }
        res = await piiApi.detectText(text, 'TEXT_INPUT');
      } else {
        if (!file) { setError('Please select a file to scan.'); setScanning(false); return; }
        res = await piiApi.detectFile(file);
      }
      onResult(res.data?.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Scan failed. Please try again.');
    } finally {
      setScanning(false);
    }
  };

  return (
    <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center gap-2">
        <ScanLine className="w-5 h-5 text-cyan-400" />
        <h3 className="font-semibold text-white">Run PII Detection</h3>
      </div>

      {/* Mode tabs */}
      <div className="flex gap-1 bg-slate-900 p-1 rounded-xl w-fit">
        {[{ id: 'text', label: 'Paste Text', Icon: FileText }, { id: 'file', label: 'Upload File', Icon: Upload }].map(({ id, label, Icon }) => (
          <button key={id} onClick={() => { setMode(id); setError(''); }}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all
              ${mode === id ? 'bg-cyan-600 text-white' : 'text-slate-400 hover:text-white'}`}>
            <Icon className="w-3.5 h-3.5" /> {label}
          </button>
        ))}
      </div>

      {/* Input area */}
      {mode === 'text' ? (
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          rows={5}
          placeholder="Paste or type any text to scan for PII&#10;&#10;Example: John Doe, DOB: 01/01/1990, Aadhaar: 2345 6789 0123, Email: john@example.com"
          className="w-full px-4 py-3 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-200 placeholder-slate-600 font-mono focus:border-cyan-500 outline-none resize-none"
        />
      ) : (
        <div className="relative">
          <label className="block w-full border-2 border-dashed border-slate-700 hover:border-cyan-500/60 rounded-xl p-8 text-center cursor-pointer transition-all">
            <input type="file" className="hidden" accept=".txt,.csv,.json,.xml,.xlsx"
              onChange={(e) => setFile(e.target.files[0])} />
            {file ? (
              <div className="space-y-1">
                <FileText className="w-8 h-8 text-cyan-400 mx-auto" />
                <p className="text-white text-sm font-semibold">{file.name}</p>
                <p className="text-slate-400 text-xs">{(file.size / 1024).toFixed(1)} KB</p>
                <button onClick={(e) => { e.preventDefault(); setFile(null); }}
                  className="text-rose-400 hover:text-rose-300 text-xs flex items-center gap-1 mx-auto">
                  <X className="w-3 h-3" /> Remove
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                <Upload className="w-8 h-8 text-slate-500 mx-auto" />
                <p className="text-slate-400 text-sm">Click to upload a file</p>
                <p className="text-slate-600 text-xs">Supported: TXT, CSV, JSON, XML, XLSX</p>
              </div>
            )}
          </label>
        </div>
      )}

      {error && (
        <div className="p-3 bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs rounded-xl">{error}</div>
      )}

      <button onClick={handleScan} disabled={scanning}
        className="w-full py-2.5 bg-gradient-to-r from-cyan-600 to-blue-700 hover:from-cyan-500 hover:to-blue-600 text-white rounded-xl text-sm font-semibold flex items-center justify-center gap-2 transition-all disabled:opacity-50">
        {scanning
          ? <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          : <Play className="w-4 h-4 fill-current" />}
        {scanning ? 'Scanning...' : 'Start PII Detection'}
      </button>
    </div>
  );
};

export default DetectionPanel;
