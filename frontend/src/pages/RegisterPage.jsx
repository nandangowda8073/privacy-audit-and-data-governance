import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { UserPlus, ShieldCheck, FileSearch, Lock, CheckCircle2, AlertCircle, ArrowLeft } from 'lucide-react';

export const RegisterPage = () => {
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    role: 'ROLE_SECURITY_OFFICER'
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsSubmitting(true);

    const result = await register(formData);
    setIsSubmitting(false);

    if (result.success) {
      setSuccess(result.message);
      setTimeout(() => navigate('/login'), 2000);
    } else {
      setError(result.message);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="w-full max-w-xl">
        <div className="glass-panel p-8 rounded-3xl border border-slate-800 shadow-2xl relative">
          
          <Link to="/login" className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-cyan-400 mb-6 transition-colors font-mono">
            <ArrowLeft className="w-3.5 h-3.5" /> Back to Login
          </Link>

          <div className="mb-6 space-y-1">
            <h2 className="text-2xl font-bold text-white flex items-center gap-2">
              <UserPlus className="w-6 h-6 text-cyan-400" />
              Register Platform Account
            </h2>
            <p className="text-xs text-slate-400">Create a user account with role-based platform permissions</p>
          </div>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs flex items-center gap-3">
              <AlertCircle className="w-5 h-5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="mb-6 p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center gap-3">
              <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
              <span>{success} Redirecting to login...</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">Full Name</label>
              <input
                type="text"
                name="fullName"
                required
                value={formData.fullName}
                onChange={handleChange}
                placeholder="Dr. Elena Vance"
                className="w-full px-4 py-2.5 rounded-xl bg-slate-950/80 border border-slate-800 text-white text-sm focus:outline-none focus:border-cyan-500 transition-all"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Username</label>
                <input
                  type="text"
                  name="username"
                  required
                  value={formData.username}
                  onChange={handleChange}
                  placeholder="elena_vance"
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-950/80 border border-slate-800 text-white text-sm focus:outline-none focus:border-cyan-500 font-mono transition-all"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Email Address</label>
                <input
                  type="email"
                  name="email"
                  required
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="elena@piiscan.com"
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-950/80 border border-slate-800 text-white text-sm focus:outline-none focus:border-cyan-500 font-mono transition-all"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">Password</label>
              <input
                type="password"
                name="password"
                required
                minLength={6}
                value={formData.password}
                onChange={handleChange}
                placeholder="At least 6 characters"
                className="w-full px-4 py-2.5 rounded-xl bg-slate-950/80 border border-slate-800 text-white text-sm focus:outline-none focus:border-cyan-500 font-mono transition-all"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-2">Select Account Role</label>
              <div className="grid grid-cols-2 gap-3">
                <label className={`p-3 rounded-xl border cursor-pointer transition-all flex items-center gap-3 ${formData.role === 'ROLE_SECURITY_OFFICER' ? 'bg-cyan-500/10 border-cyan-500 text-cyan-300' : 'bg-slate-950/40 border-slate-800 text-slate-400'}`}>
                  <input
                    type="radio"
                    name="role"
                    value="ROLE_SECURITY_OFFICER"
                    checked={formData.role === 'ROLE_SECURITY_OFFICER'}
                    onChange={handleChange}
                    className="hidden"
                  />
                  <ShieldCheck className="w-5 h-5 text-cyan-400 flex-shrink-0" />
                  <div>
                    <div className="text-xs font-semibold text-white">Security Officer</div>
                    <div className="text-[10px] text-slate-400">Scan & Remediation</div>
                  </div>
                </label>

                <label className={`p-3 rounded-xl border cursor-pointer transition-all flex items-center gap-3 ${formData.role === 'ROLE_AUDITOR' ? 'bg-purple-500/10 border-purple-500 text-purple-300' : 'bg-slate-950/40 border-slate-800 text-slate-400'}`}>
                  <input
                    type="radio"
                    name="role"
                    value="ROLE_AUDITOR"
                    checked={formData.role === 'ROLE_AUDITOR'}
                    onChange={handleChange}
                    className="hidden"
                  />
                  <FileSearch className="w-5 h-5 text-purple-400 flex-shrink-0" />
                  <div>
                    <div className="text-xs font-semibold text-white">Compliance Auditor</div>
                    <div className="text-[10px] text-slate-400">Audit Logs & Reports</div>
                  </div>
                </label>
              </div>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full mt-2 py-3 px-6 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-semibold text-sm shadow-lg shadow-cyan-500/25 transition-all disabled:opacity-50"
            >
              {isSubmitting ? 'Creating User Account...' : 'Complete Registration'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
