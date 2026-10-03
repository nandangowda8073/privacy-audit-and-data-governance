import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Shield, Eye, EyeOff, Lock, UserCheck, AlertCircle, ArrowRight, CheckCircle2 } from 'lucide-react';

export const LoginPage = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    const result = await login(usernameOrEmail, password);
    setIsSubmitting(false);

    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.message);
    }
  };

  const handleQuickLogin = (demoEmail, demoPassword) => {
    setUsernameOrEmail(demoEmail);
    setPassword(demoPassword);
    setError('');
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="w-full max-w-4xl grid md:grid-cols-12 gap-6 items-center">
        
        {/* Left Column: Platform info & Quick Account Selection */}
        <div className="md:col-span-5 space-y-6">
          <div className="space-y-3">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 text-xs font-mono">
              <Shield className="w-3.5 h-3.5" />
              <span>Module 1: Auth & User Security</span>
            </div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight leading-tight">
              Privacy Audit & PII Scanning Platform
            </h1>
            <p className="text-sm text-slate-400 leading-relaxed">
              Automated data privacy governance, PII scanning engine, and RBAC security controls.
            </p>
          </div>

          {/* Quick Login Presets for easy testing */}
          <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-3">
            <span className="text-xs font-mono uppercase tracking-wider text-slate-400 font-semibold block">
              Pre-configured Test Roles:
            </span>
            
            <div className="space-y-2">
              <button
                type="button"
                onClick={() => handleQuickLogin('admin@piiscan.com', 'Admin@123')}
                className="w-full text-left p-2.5 rounded-xl bg-slate-900/60 hover:bg-slate-800/80 border border-rose-500/30 transition-all flex items-center justify-between group"
              >
                <div>
                  <div className="text-xs font-semibold text-rose-400 flex items-center gap-1.5">
                    <UserCheck className="w-3.5 h-3.5" /> Super Admin
                  </div>
                  <div className="text-[11px] font-mono text-slate-400">admin@piiscan.com</div>
                </div>
                <span className="text-xs text-slate-500 group-hover:text-rose-400 font-mono">Select &rarr;</span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('secops@piiscan.com', 'SecOps@123')}
                className="w-full text-left p-2.5 rounded-xl bg-slate-900/60 hover:bg-slate-800/80 border border-cyan-500/30 transition-all flex items-center justify-between group"
              >
                <div>
                  <div className="text-xs font-semibold text-cyan-400 flex items-center gap-1.5">
                    <UserCheck className="w-3.5 h-3.5" /> Security Officer
                  </div>
                  <div className="text-[11px] font-mono text-slate-400">secops@piiscan.com</div>
                </div>
                <span className="text-xs text-slate-500 group-hover:text-cyan-400 font-mono">Select &rarr;</span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('auditor@piiscan.com', 'Auditor@123')}
                className="w-full text-left p-2.5 rounded-xl bg-slate-900/60 hover:bg-slate-800/80 border border-purple-500/30 transition-all flex items-center justify-between group"
              >
                <div>
                  <div className="text-xs font-semibold text-purple-400 flex items-center gap-1.5">
                    <UserCheck className="w-3.5 h-3.5" /> Compliance Auditor
                  </div>
                  <div className="text-[11px] font-mono text-slate-400">auditor@piiscan.com</div>
                </div>
                <span className="text-xs text-slate-500 group-hover:text-purple-400 font-mono">Select &rarr;</span>
              </button>
            </div>
          </div>
        </div>

        {/* Right Column: Main Login Form */}
        <div className="md:col-span-7">
          <div className="glass-panel p-8 rounded-3xl border border-slate-800 shadow-2xl relative overflow-hidden">
            <div className="absolute top-0 right-0 w-32 h-32 bg-cyan-500/10 rounded-full blur-3xl -z-10 pointer-events-none"></div>

            <div className="mb-6 space-y-1">
              <h2 className="text-2xl font-bold text-white flex items-center gap-2">
                <Lock className="w-6 h-6 text-cyan-400" />
                Sign In to Platform
              </h2>
              <p className="text-xs text-slate-400">Enter your official credentials or select a role preset</p>
            </div>

            {error && (
              <div className="mb-6 p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs flex items-center gap-3 animate-shake">
                <AlertCircle className="w-5 h-5 flex-shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-2">
                  Username or Email Address
                </label>
                <input
                  type="text"
                  required
                  value={usernameOrEmail}
                  onChange={(e) => setUsernameOrEmail(e.target.value)}
                  placeholder="admin@piiscan.com"
                  className="w-full px-4 py-3 rounded-xl bg-slate-950/80 border border-slate-800 text-white text-sm focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition-all font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-2">
                  Password
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••••••"
                    className="w-full px-4 py-3 rounded-xl bg-slate-950/80 border border-slate-800 text-white text-sm focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition-all font-mono pr-10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3.5 px-6 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-semibold text-sm shadow-lg shadow-cyan-500/25 flex items-center justify-center gap-2 transition-all disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span className="flex items-center gap-2">
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                    Authenticating JWT...
                  </span>
                ) : (
                  <>
                    <span>Authenticate Account</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>

            <div className="mt-6 pt-6 border-t border-slate-800/80 text-center">
              <p className="text-xs text-slate-400">
                Need a new user profile?{' '}
                <Link to="/register" className="text-cyan-400 hover:underline font-medium">
                  Register User &rarr;
                </Link>
              </p>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};
