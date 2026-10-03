import React, { useState, useEffect, useMemo } from 'react';
import { 
  Database, Search, Plus, RefreshCw, Server, AlertTriangle, 
  CheckCircle2, XCircle, Slash, Eye, Edit3, Trash2, Zap, 
  ToggleLeft, ToggleRight, X, Shield, Lock, Clock, Info, 
  Filter, ChevronDown, Check, ArrowUpDown, Cpu, HardDrive
} from 'lucide-react';
import { databaseApi } from '../api/databaseApi';
import { useAuth } from '../context/AuthContext';

export const DatabaseManagementPage = () => {
  const { hasRole, user } = useAuth();

  // Role permissions
  const isSuperAdmin = hasRole('ROLE_SUPER_ADMIN');
  const isSecurityOfficer = hasRole(['ROLE_SUPER_ADMIN', 'ROLE_SECURITY_OFFICER']);
  const isAuditor = hasRole('ROLE_AUDITOR');

  // Main state
  const [databases, setDatabases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Search and filter states
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDbType, setSelectedDbType] = useState('ALL'); // ALL, MYSQL, POSTGRESQL
  const [selectedStatus, setSelectedStatus] = useState('ALL'); // ALL, CONNECTED, DISCONNECTED
  const [selectedEnabled, setSelectedEnabled] = useState('ALL'); // ALL, ENABLED, DISABLED

  // Modal states
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [editingDb, setEditingDb] = useState(null); // null = Add Mode, Object = Edit Mode
  const [formErrors, setFormErrors] = useState({});

  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [viewingDb, setViewingDb] = useState(null);

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deletingDb, setDeletingDb] = useState(null);

  const [isTestResultModalOpen, setIsTestResultModalOpen] = useState(false);
  const [testResult, setTestResult] = useState(null);
  const [testingId, setTestingId] = useState(null);

  // Form Fields State
  const [formData, setFormData] = useState({
    connectionName: '',
    databaseType: 'MYSQL',
    host: 'localhost',
    port: 3306,
    databaseName: '',
    username: '',
    password: '',
    description: ''
  });

  // Action Loading states
  const [actionLoading, setActionLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // Fetch Databases from API
  const fetchDatabases = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await databaseApi.getAll();
      if (response.data && response.data.success) {
        setDatabases(response.data.data || []);
      } else {
        setDatabases(response.data || []);
      }
    } catch (err) {
      console.error("Error loading database connections:", err);
      setError(err.response?.data?.message || 'Failed to fetch database connections. Please verify backend service.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDatabases();
  }, []);

  // Filtered and Searched Databases
  const filteredDatabases = useMemo(() => {
    return databases.filter((db) => {
      // Search by Connection Name or Host
      const matchesSearch = !searchQuery.trim() || 
        db.connectionName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        db.host?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        db.databaseName?.toLowerCase().includes(searchQuery.toLowerCase());

      // Filter by DB Type
      const matchesType = selectedDbType === 'ALL' || db.databaseType === selectedDbType;

      // Filter by Status
      const matchesStatus = selectedStatus === 'ALL' || db.connectionStatus === selectedStatus;

      // Filter by Enabled
      const matchesEnabled = selectedEnabled === 'ALL' || 
        (selectedEnabled === 'ENABLED' && db.enabled) ||
        (selectedEnabled === 'DISABLED' && !db.enabled);

      return matchesSearch && matchesType && matchesStatus && matchesEnabled;
    });
  }, [databases, searchQuery, selectedDbType, selectedStatus, selectedEnabled]);

  // Statistics summary
  const stats = useMemo(() => {
    const total = databases.length;
    const connected = databases.filter(d => d.enabled && d.connectionStatus === 'CONNECTED').length;
    const disconnected = databases.filter(d => d.enabled && d.connectionStatus === 'DISCONNECTED').length;
    const disabled = databases.filter(d => !d.enabled).length;
    const mysqlCount = databases.filter(d => d.databaseType === 'MYSQL').length;
    const postgresCount = databases.filter(d => d.databaseType === 'POSTGRESQL').length;
    return { total, connected, disconnected, disabled, mysqlCount, postgresCount };
  }, [databases]);

  // Form Handlers
  const handleOpenAddModal = () => {
    setEditingDb(null);
    setFormData({
      connectionName: '',
      databaseType: 'MYSQL',
      host: 'localhost',
      port: 3306,
      databaseName: '',
      username: '',
      password: '',
      description: ''
    });
    setFormErrors({});
    setIsFormModalOpen(true);
  };

  const handleOpenEditModal = (db) => {
    setEditingDb(db);
    setFormData({
      connectionName: db.connectionName || '',
      databaseType: db.databaseType || 'MYSQL',
      host: db.host || '',
      port: db.port || (db.databaseType === 'POSTGRESQL' ? 5432 : 3306),
      databaseName: db.databaseName || '',
      username: db.username || '',
      password: '', // Blank by default, updated only if provided
      description: db.description || ''
    });
    setFormErrors({});
    setIsFormModalOpen(true);
  };

  const handleDbTypeChange = (e) => {
    const type = e.target.value;
    setFormData(prev => ({
      ...prev,
      databaseType: type,
      port: type === 'POSTGRESQL' ? 5432 : 3306
    }));
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.connectionName.trim()) errors.connectionName = 'Connection name is required';
    if (!formData.host.trim()) errors.host = 'Host address is required';
    if (!formData.port || formData.port < 1 || formData.port > 65535) errors.port = 'Port must be between 1 and 65535';
    if (!formData.databaseName.trim()) errors.databaseName = 'Database name is required';
    if (!formData.username.trim()) errors.username = 'Username is required';
    if (!editingDb && !formData.password.trim()) errors.password = 'Password is required for new connection';

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setActionLoading(true);
    try {
      if (editingDb) {
        // Update connection
        const payload = { ...formData };
        if (!payload.password.trim()) delete payload.password; // Do not overwrite if blank

        const res = await databaseApi.update(editingDb.id, payload);
        if (res.data?.success || res.status === 200) {
          showToast(`Database '${formData.connectionName}' updated successfully`, 'success');
          setIsFormModalOpen(false);
          fetchDatabases();
        }
      } else {
        // Create connection
        const res = await databaseApi.create(formData);
        if (res.data?.success || res.status === 201) {
          showToast(`Database '${formData.connectionName}' created successfully`, 'success');
          setIsFormModalOpen(false);
          fetchDatabases();
        }
      }
    } catch (err) {
      console.error("Form submit error:", err);
      const msg = err.response?.data?.message || 'Failed to save database connection.';
      showToast(msg, 'error');
    } finally {
      setActionLoading(false);
    }
  };

  // Test Connection
  const handleTestConnection = async (id, name) => {
    setTestingId(id);
    try {
      const res = await databaseApi.testConnection(id);
      const data = res.data?.data || res.data;
      setTestResult({
        dbName: name,
        success: data.success,
        message: data.message || (data.success ? 'Connection Successful' : 'Connection Failed'),
        databaseVersion: data.databaseVersion || 'Unknown / N/A',
        responseTimeMs: data.responseTimeMs !== undefined ? data.responseTimeMs : 0,
        timestamp: data.timestamp ? new Date(data.timestamp).toLocaleString() : new Date().toLocaleString()
      });
      setIsTestResultModalOpen(true);
      fetchDatabases();
    } catch (err) {
      console.error("Test connection error:", err);
      const msg = err.response?.data?.message || 'Connection attempt failed or host unreachable.';
      setTestResult({
        dbName: name,
        success: false,
        message: msg,
        databaseVersion: 'N/A',
        responseTimeMs: 0,
        timestamp: new Date().toLocaleString()
      });
      setIsTestResultModalOpen(true);
    } finally {
      setTestingId(null);
    }
  };

  // Enable / Disable Database
  const handleToggleEnable = async (db) => {
    setActionLoading(true);
    try {
      if (db.enabled) {
        await databaseApi.disable(db.id);
        showToast(`Database '${db.connectionName}' disabled`, 'info');
      } else {
        await databaseApi.enable(db.id);
        showToast(`Database '${db.connectionName}' enabled`, 'success');
      }
      fetchDatabases();
    } catch (err) {
      console.error("Toggle enable error:", err);
      showToast(err.response?.data?.message || 'Failed to toggle connection status', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  // Delete Database
  const handleDeleteConfirm = async () => {
    if (!deletingDb) return;
    setActionLoading(true);
    try {
      await databaseApi.remove(deletingDb.id);
      showToast(`Database '${deletingDb.connectionName}' deleted permanently`, 'success');
      setIsDeleteModalOpen(false);
      setDeletingDb(null);
      fetchDatabases();
    } catch (err) {
      console.error("Delete error:", err);
      showToast(err.response?.data?.message || 'Failed to delete database connection', 'error');
    } finally {
      setActionLoading(false);
    }
  };

  // Status Badge Component
  const renderStatusBadge = (db) => {
    if (!db.enabled) {
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-slate-800/80 text-slate-400 border border-slate-700">
          <span className="w-1.5 h-1.5 rounded-full bg-slate-500"></span>
          Disabled
        </span>
      );
    }
    if (db.connectionStatus === 'CONNECTED') {
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
          Connected
        </span>
      );
    }
    if (db.connectionStatus === 'DISCONNECTED') {
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-rose-500/10 text-rose-400 border border-rose-500/30">
          <span className="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
          Disconnected
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-500/10 text-amber-400 border border-amber-500/30">
        <span className="w-1.5 h-1.5 rounded-full bg-amber-400"></span>
        Unknown
      </span>
    );
  };

  // DB Type Badge Component
  const renderDbTypeBadge = (type) => {
    const isPg = type === 'POSTGRESQL';
    return (
      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-mono font-semibold border ${
        isPg 
          ? 'bg-blue-500/10 text-blue-300 border-blue-500/30' 
          : 'bg-amber-500/10 text-amber-300 border-amber-500/30'
      }`}>
        <Server className={`w-3.5 h-3.5 ${isPg ? 'text-blue-400' : 'text-amber-400'}`} />
        {type}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      
      {/* Toast Notification */}
      {toast && (
        <div className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 rounded-2xl border shadow-2xl backdrop-blur-xl transition-all duration-300 ${
          toast.type === 'error'
            ? 'bg-rose-950/90 border-rose-500/50 text-rose-200'
            : toast.type === 'info'
            ? 'bg-slate-900/90 border-cyan-500/50 text-cyan-200'
            : 'bg-emerald-950/90 border-emerald-500/50 text-emerald-200'
        }`}>
          {toast.type === 'error' ? <XCircle className="w-5 h-5 text-rose-400" /> : <CheckCircle2 className="w-5 h-5 text-emerald-400" />}
          <span className="text-xs font-medium">{toast.message}</span>
          <button onClick={() => setToast(null)} className="text-slate-400 hover:text-white ml-2">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Header & Subtitle */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 glass-panel p-6 rounded-3xl border border-slate-800/80 bg-gradient-to-r from-[#0d131f] to-[#090d16]">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <div className="p-2.5 bg-cyan-500/10 border border-cyan-500/30 rounded-2xl text-cyan-400">
              <Database className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-2xl font-extrabold text-white tracking-tight">Database Management</h1>
              <p className="text-xs text-slate-400 font-mono mt-0.5">Manage enterprise database connections securely.</p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={fetchDatabases}
            disabled={loading}
            className="px-3.5 py-2.5 rounded-xl bg-slate-900/80 hover:bg-slate-800 border border-slate-800 text-xs font-mono text-cyan-300 flex items-center gap-2 transition-all shadow-sm"
            title="Reload Connection List"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          {isSuperAdmin && (
            <button
              onClick={handleOpenAddModal}
              className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold text-xs flex items-center gap-2 shadow-lg shadow-cyan-500/20 transition-all cursor-pointer"
            >
              <Plus className="w-4 h-4 stroke-[3]" /> Add Database
            </button>
          )}
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="glass-panel p-4 rounded-2xl border border-slate-800/80 bg-slate-900/40 flex items-center justify-between">
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Total Targets</p>
            <p className="text-2xl font-black text-white mt-1">{stats.total}</p>
            <p className="text-[10px] text-slate-500 font-mono mt-0.5">{stats.mysqlCount} MySQL · {stats.postgresCount} Postgres</p>
          </div>
          <div className="w-10 h-10 bg-cyan-500/10 border border-cyan-500/20 rounded-xl flex items-center justify-center text-cyan-400">
            <HardDrive className="w-5 h-5" />
          </div>
        </div>

        <div className="glass-panel p-4 rounded-2xl border border-slate-800/80 bg-slate-900/40 flex items-center justify-between">
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Connected</p>
            <p className="text-2xl font-black text-emerald-400 mt-1">{stats.connected}</p>
            <p className="text-[10px] text-emerald-500/80 font-mono mt-0.5">Active & Reachable</p>
          </div>
          <div className="w-10 h-10 bg-emerald-500/10 border border-emerald-500/20 rounded-xl flex items-center justify-center text-emerald-400">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>

        <div className="glass-panel p-4 rounded-2xl border border-slate-800/80 bg-slate-900/40 flex items-center justify-between">
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Disconnected</p>
            <p className="text-2xl font-black text-rose-400 mt-1">{stats.disconnected}</p>
            <p className="text-[10px] text-rose-500/80 font-mono mt-0.5">Connection Failed</p>
          </div>
          <div className="w-10 h-10 bg-rose-500/10 border border-rose-500/20 rounded-xl flex items-center justify-center text-rose-400">
            <XCircle className="w-5 h-5" />
          </div>
        </div>

        <div className="glass-panel p-4 rounded-2xl border border-slate-800/80 bg-slate-900/40 flex items-center justify-between">
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Disabled</p>
            <p className="text-2xl font-black text-slate-400 mt-1">{stats.disabled}</p>
            <p className="text-[10px] text-slate-500 font-mono mt-0.5">Scanning Suspended</p>
          </div>
          <div className="w-10 h-10 bg-slate-800/60 border border-slate-700/60 rounded-xl flex items-center justify-center text-slate-400">
            <Slash className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Toolbar: Search & Filters */}
      <div className="glass-panel p-4 rounded-2xl border border-slate-800 space-y-3">
        <div className="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3">
          
          {/* Search Box */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by Connection Name or Host..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-9 py-2.5 bg-slate-950/80 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500/60 transition-all font-mono"
            />
            {searchQuery && (
              <button 
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Filters Group */}
          <div className="flex flex-wrap items-center gap-2">
            
            {/* Filter by DB Type */}
            <div className="flex items-center bg-slate-950/80 border border-slate-800 rounded-xl p-1 text-xs">
              <span className="text-[11px] font-mono text-slate-500 px-2 flex items-center gap-1">
                <Filter className="w-3 h-3 text-cyan-400" /> Type:
              </span>
              {['ALL', 'MYSQL', 'POSTGRESQL'].map((t) => (
                <button
                  key={t}
                  onClick={() => setSelectedDbType(t)}
                  className={`px-2.5 py-1 rounded-lg font-mono text-[11px] transition-all ${
                    selectedDbType === t
                      ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-semibold'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  {t === 'ALL' ? 'All Types' : t}
                </button>
              ))}
            </div>

            {/* Filter by Status */}
            <div className="flex items-center bg-slate-950/80 border border-slate-800 rounded-xl p-1 text-xs">
              <span className="text-[11px] font-mono text-slate-500 px-2">Status:</span>
              {[
                { key: 'ALL', label: 'All' },
                { key: 'CONNECTED', label: 'Connected' },
                { key: 'DISCONNECTED', label: 'Disconnected' }
              ].map((s) => (
                <button
                  key={s.key}
                  onClick={() => setSelectedStatus(s.key)}
                  className={`px-2.5 py-1 rounded-lg font-mono text-[11px] transition-all ${
                    selectedStatus === s.key
                      ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-semibold'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  {s.label}
                </button>
              ))}
            </div>

            {/* Filter by Enabled */}
            <div className="flex items-center bg-slate-950/80 border border-slate-800 rounded-xl p-1 text-xs">
              <span className="text-[11px] font-mono text-slate-500 px-2">State:</span>
              {[
                { key: 'ALL', label: 'All' },
                { key: 'ENABLED', label: 'Enabled' },
                { key: 'DISABLED', label: 'Disabled' }
              ].map((e) => (
                <button
                  key={e.key}
                  onClick={() => setSelectedEnabled(e.key)}
                  className={`px-2.5 py-1 rounded-lg font-mono text-[11px] transition-all ${
                    selectedEnabled === e.key
                      ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-semibold'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  {e.label}
                </button>
              ))}
            </div>

            {/* Clear All Filters Button */}
            {(searchQuery || selectedDbType !== 'ALL' || selectedStatus !== 'ALL' || selectedEnabled !== 'ALL') && (
              <button
                onClick={() => {
                  setSearchQuery('');
                  setSelectedDbType('ALL');
                  setSelectedStatus('ALL');
                  setSelectedEnabled('ALL');
                }}
                className="px-2.5 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-rose-300 text-xs font-mono border border-slate-800 transition-all"
              >
                Reset
              </button>
            )}

          </div>
        </div>
      </div>

      {/* Database Connection Table */}
      <div className="glass-panel rounded-3xl border border-slate-800/80 overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-950/80 text-slate-400 font-mono text-[11px] uppercase tracking-wider">
                <th className="py-4 px-4 font-semibold">Connection Name</th>
                <th className="py-4 px-4 font-semibold">Database Type</th>
                <th className="py-4 px-4 font-semibold">Host : Port</th>
                <th className="py-4 px-4 font-semibold">Database Name</th>
                <th className="py-4 px-4 font-semibold">Username</th>
                <th className="py-4 px-4 font-semibold">Status</th>
                <th className="py-4 px-4 font-semibold">Last Tested</th>
                <th className="py-4 px-4 font-semibold text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                <tr>
                  <td colSpan="8" className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center gap-3">
                      <RefreshCw className="w-6 h-6 text-cyan-400 animate-spin" />
                      <p className="font-mono text-xs text-slate-400">Loading enterprise database connections...</p>
                    </div>
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan="8" className="py-12 text-center">
                    <div className="max-w-md mx-auto p-6 bg-rose-950/20 border border-rose-500/30 rounded-2xl text-center space-y-2">
                      <AlertTriangle className="w-8 h-8 text-rose-400 mx-auto" />
                      <h3 className="text-sm font-bold text-white">Error Loading Connections</h3>
                      <p className="text-xs text-slate-400">{error}</p>
                      <button
                        onClick={fetchDatabases}
                        className="mt-2 px-4 py-2 bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 rounded-xl text-xs font-mono border border-rose-500/40"
                      >
                        Try Again
                      </button>
                    </div>
                  </td>
                </tr>
              ) : filteredDatabases.length === 0 ? (
                <tr>
                  <td colSpan="8" className="py-16 text-center text-slate-400">
                    <div className="max-w-md mx-auto space-y-3">
                      <div className="w-14 h-14 bg-slate-900 border border-slate-800 rounded-full flex items-center justify-center mx-auto text-slate-500">
                        <Database className="w-7 h-7" />
                      </div>
                      <h3 className="text-base font-bold text-white">No Database Connections Found</h3>
                      <p className="text-xs text-slate-400 font-mono">
                        {searchQuery || selectedDbType !== 'ALL' || selectedStatus !== 'ALL' || selectedEnabled !== 'ALL'
                          ? 'No target databases matched your search or filter criteria.'
                          : 'No database connections configured yet. Click "Add Database" to connect a new database target.'}
                      </p>
                      {isSuperAdmin && !searchQuery && selectedDbType === 'ALL' && (
                        <button
                          onClick={handleOpenAddModal}
                          className="px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs inline-flex items-center gap-2 mt-2"
                        >
                          <Plus className="w-4 h-4" /> Create Database Connection
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                filteredDatabases.map((db) => (
                  <tr key={db.id} className="hover:bg-slate-900/40 transition-colors group">
                    
                    {/* Connection Name */}
                    <td className="py-3.5 px-4 font-semibold text-white">
                      <div className="flex items-center gap-2.5">
                        <div className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-cyan-400 group-hover:border-cyan-500/30 transition-colors">
                          <Database className="w-4 h-4" />
                        </div>
                        <div>
                          <div className="font-bold text-slate-100 flex items-center gap-2">
                            {db.connectionName}
                            {!db.enabled && (
                              <span className="text-[10px] bg-slate-800 text-slate-400 px-1.5 py-0.5 rounded font-mono">
                                Off
                              </span>
                            )}
                          </div>
                          {db.description && (
                            <p className="text-[10px] text-slate-400 max-w-xs truncate font-normal">
                              {db.description}
                            </p>
                          )}
                        </div>
                      </div>
                    </td>

                    {/* Database Type */}
                    <td className="py-3.5 px-4">
                      {renderDbTypeBadge(db.databaseType)}
                    </td>

                    {/* Host & Port */}
                    <td className="py-3.5 px-4 font-mono text-slate-300">
                      <div className="flex items-center gap-1">
                        <span className="text-slate-200">{db.host}</span>
                        <span className="text-cyan-400 font-bold">:{db.port}</span>
                      </div>
                    </td>

                    {/* Database Name */}
                    <td className="py-3.5 px-4 font-mono">
                      <span className="bg-slate-950 px-2 py-1 rounded border border-slate-800 text-cyan-300 text-[11px]">
                        {db.databaseName}
                      </span>
                    </td>

                    {/* Username */}
                    <td className="py-3.5 px-4 font-mono text-slate-300">
                      {db.username}
                    </td>

                    {/* Status */}
                    <td className="py-3.5 px-4">
                      {renderStatusBadge(db)}
                    </td>

                    {/* Last Tested */}
                    <td className="py-3.5 px-4 font-mono text-[11px] text-slate-400">
                      {db.lastConnectionTest ? (
                        <div>
                          <div className="text-slate-300">{new Date(db.lastConnectionTest).toLocaleString()}</div>
                          {db.lastResponseTimeMs !== undefined && db.lastResponseTimeMs !== null && (
                            <div className="text-[10px] text-cyan-400 font-mono mt-0.5">
                              ⏱️ {db.lastResponseTimeMs} ms
                            </div>
                          )}
                        </div>
                      ) : (
                        <span className="text-slate-500 font-mono italic">Never tested</span>
                      )}
                    </td>

                    {/* Actions */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        
                        {/* View Details */}
                        <button
                          onClick={() => {
                            setViewingDb(db);
                            setIsDetailsModalOpen(true);
                          }}
                          className="p-1.5 rounded-lg bg-slate-900/80 hover:bg-slate-800 text-slate-300 hover:text-cyan-300 border border-slate-800 transition-all"
                          title="View Connection Details"
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </button>

                        {/* Test Connection */}
                        {isSecurityOfficer && (
                          <button
                            onClick={() => handleTestConnection(db.id, db.connectionName)}
                            disabled={testingId === db.id || !db.enabled}
                            className={`p-1.5 rounded-lg border transition-all ${
                              testingId === db.id
                                ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                                : !db.enabled
                                ? 'bg-slate-950 border-slate-900 text-slate-600 cursor-not-allowed'
                                : 'bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-300 border-cyan-500/30'
                            }`}
                            title={!db.enabled ? "Cannot test disabled database" : "Test Live Connection"}
                          >
                            <Zap className={`w-3.5 h-3.5 ${testingId === db.id ? 'animate-bounce' : ''}`} />
                          </button>
                        )}

                        {/* Enable / Disable */}
                        {isSuperAdmin && (
                          <button
                            onClick={() => handleToggleEnable(db)}
                            disabled={actionLoading}
                            className={`p-1.5 rounded-lg border transition-all ${
                              db.enabled 
                                ? 'bg-slate-900/80 hover:bg-slate-800 text-emerald-400 border-slate-800' 
                                : 'bg-amber-500/10 text-amber-400 border-amber-500/30 hover:bg-amber-500/20'
                            }`}
                            title={db.enabled ? "Disable Connection" : "Enable Connection"}
                          >
                            {db.enabled ? <ToggleRight className="w-3.5 h-3.5 text-emerald-400" /> : <ToggleLeft className="w-3.5 h-3.5 text-amber-400" />}
                          </button>
                        )}

                        {/* Edit */}
                        {isSuperAdmin && (
                          <button
                            onClick={() => handleOpenEditModal(db)}
                            className="p-1.5 rounded-lg bg-slate-900/80 hover:bg-slate-800 text-slate-300 hover:text-blue-400 border border-slate-800 transition-all"
                            title="Edit Connection Info"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                          </button>
                        )}

                        {/* Delete */}
                        {isSuperAdmin && (
                          <button
                            onClick={() => {
                              setDeletingDb(db);
                              setIsDeleteModalOpen(true);
                            }}
                            className="p-1.5 rounded-lg bg-slate-900/80 hover:bg-rose-950/40 text-slate-400 hover:text-rose-400 border border-slate-800 hover:border-rose-500/30 transition-all"
                            title="Delete Connection"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}

                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* ── MODAL 1: ADD / EDIT DATABASE ────────────────────────────────────── */}
      {isFormModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
          <div className="w-full max-w-xl glass-panel rounded-3xl border border-slate-800 bg-[#0c1019] shadow-2xl overflow-hidden">
            
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800/80 bg-slate-950/50">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-cyan-500/10 border border-cyan-500/30 rounded-xl text-cyan-400">
                  <Database className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">
                    {editingDb ? `Edit Database Connection (${editingDb.connectionName})` : 'Add New Database Connection'}
                  </h3>
                  <p className="text-xs text-slate-400 font-mono">
                    {editingDb ? 'Modify target database connection credentials' : 'Configure enterprise database credentials securely'}
                  </p>
                </div>
              </div>
              <button 
                onClick={() => setIsFormModalOpen(false)}
                className="p-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-white border border-slate-800"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Modal Form */}
            <form onSubmit={handleFormSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto">
              
              {/* Connection Name & Database Type */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                    Connection Name <span className="text-rose-400">*</span>
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Production_Users_DB"
                    value={formData.connectionName}
                    onChange={(e) => setFormData({ ...formData, connectionName: e.target.value })}
                    className={`w-full px-3.5 py-2 bg-slate-950 border rounded-xl text-xs text-white placeholder-slate-600 font-mono focus:outline-none transition-all ${
                      formErrors.connectionName ? 'border-rose-500' : 'border-slate-800 focus:border-cyan-500'
                    }`}
                  />
                  {formErrors.connectionName && <p className="text-[10px] text-rose-400 font-mono mt-1">{formErrors.connectionName}</p>}
                </div>

                <div>
                  <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                    Database Type <span className="text-rose-400">*</span>
                  </label>
                  <select
                    value={formData.databaseType}
                    onChange={handleDbTypeChange}
                    className="w-full px-3.5 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-cyan-300 font-mono focus:outline-none focus:border-cyan-500 transition-all cursor-pointer"
                  >
                    <option value="MYSQL">MySQL (Port 3306)</option>
                    <option value="POSTGRESQL">PostgreSQL (Port 5432)</option>
                  </select>
                </div>
              </div>

              {/* Host & Port */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="md:col-span-2">
                  <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                    Host / IP Address <span className="text-rose-400">*</span>
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. 192.168.1.100 or db.internal.net"
                    value={formData.host}
                    onChange={(e) => setFormData({ ...formData, host: e.target.value })}
                    className={`w-full px-3.5 py-2 bg-slate-950 border rounded-xl text-xs text-white placeholder-slate-600 font-mono focus:outline-none transition-all ${
                      formErrors.host ? 'border-rose-500' : 'border-slate-800 focus:border-cyan-500'
                    }`}
                  />
                  {formErrors.host && <p className="text-[10px] text-rose-400 font-mono mt-1">{formErrors.host}</p>}
                </div>

                <div>
                  <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                    Port <span className="text-rose-400">*</span>
                  </label>
                  <input
                    type="number"
                    placeholder="3306"
                    value={formData.port}
                    onChange={(e) => setFormData({ ...formData, port: parseInt(e.target.value) || '' })}
                    className={`w-full px-3.5 py-2 bg-slate-950 border rounded-xl text-xs text-white placeholder-slate-600 font-mono focus:outline-none transition-all ${
                      formErrors.port ? 'border-rose-500' : 'border-slate-800 focus:border-cyan-500'
                    }`}
                  />
                  {formErrors.port && <p className="text-[10px] text-rose-400 font-mono mt-1">{formErrors.port}</p>}
                </div>
              </div>

              {/* Database Name */}
              <div>
                <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                  Database Name <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  placeholder="e.g. customer_records"
                  value={formData.databaseName}
                  onChange={(e) => setFormData({ ...formData, databaseName: e.target.value })}
                  className={`w-full px-3.5 py-2 bg-slate-950 border rounded-xl text-xs text-white placeholder-slate-600 font-mono focus:outline-none transition-all ${
                    formErrors.databaseName ? 'border-rose-500' : 'border-slate-800 focus:border-cyan-500'
                  }`}
                />
                {formErrors.databaseName && <p className="text-[10px] text-rose-400 font-mono mt-1">{formErrors.databaseName}</p>}
              </div>

              {/* Username & Password */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                    Username <span className="text-rose-400">*</span>
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. db_admin"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                    className={`w-full px-3.5 py-2 bg-slate-950 border rounded-xl text-xs text-white placeholder-slate-600 font-mono focus:outline-none transition-all ${
                      formErrors.username ? 'border-rose-500' : 'border-slate-800 focus:border-cyan-500'
                    }`}
                  />
                  {formErrors.username && <p className="text-[10px] text-rose-400 font-mono mt-1">{formErrors.username}</p>}
                </div>

                <div>
                  <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                    Password {editingDb ? <span className="text-slate-500 font-normal">(Leave blank to keep existing)</span> : <span className="text-rose-400">*</span>}
                  </label>
                  <div className="relative">
                    <input
                      type="password"
                      placeholder="••••••••••••"
                      value={formData.password}
                      onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                      className={`w-full px-3.5 py-2 bg-slate-950 border rounded-xl text-xs text-white placeholder-slate-600 font-mono focus:outline-none transition-all ${
                        formErrors.password ? 'border-rose-500' : 'border-slate-800 focus:border-cyan-500'
                      }`}
                    />
                    <Lock className="w-3.5 h-3.5 text-slate-500 absolute right-3 top-1/2 -translate-y-1/2" />
                  </div>
                  {formErrors.password && <p className="text-[10px] text-rose-400 font-mono mt-1">{formErrors.password}</p>}
                </div>
              </div>

              {/* Encryption Banner */}
              <div className="p-3 bg-cyan-950/30 border border-cyan-500/20 rounded-xl flex items-start gap-2 text-[11px] text-cyan-300">
                <Lock className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                <p>
                  <strong className="text-cyan-200 font-semibold">AES-256 Encryption Security:</strong> Database credentials will be encrypted before storage and decrypted temporarily only during connection checks.
                </p>
              </div>

              {/* Description */}
              <div>
                <label className="block text-xs font-mono text-slate-300 font-semibold mb-1">
                  Description / Purpose
                </label>
                <textarea
                  rows="2"
                  placeholder="Optional description or environment tag (e.g. Staging cluster for PII audit)..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-3.5 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-600 focus:outline-none focus:border-cyan-500 transition-all font-mono"
                />
              </div>

              {/* Buttons */}
              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsFormModalOpen(false)}
                  className="px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-white text-xs font-mono border border-slate-800 transition-all"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={actionLoading}
                  className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold text-xs flex items-center gap-2 shadow-lg shadow-cyan-500/20 transition-all"
                >
                  {actionLoading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Check className="w-4 h-4 stroke-[3]" />}
                  {editingDb ? 'Save Changes' : 'Save Database'}
                </button>
              </div>

            </form>
          </div>
        </div>
      )}

      {/* ── MODAL 2: DATABASE DETAILS VIEW ──────────────────────────────────── */}
      {isDetailsModalOpen && viewingDb && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
          <div className="w-full max-w-lg glass-panel rounded-3xl border border-slate-800 bg-[#0c1019] shadow-2xl overflow-hidden">
            
            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800/80 bg-slate-950/50">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-cyan-500/10 border border-cyan-500/30 rounded-xl text-cyan-400">
                  <Eye className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">{viewingDb.connectionName}</h3>
                  <p className="text-xs text-slate-400 font-mono">Detailed Target Specification</p>
                </div>
              </div>
              <button 
                onClick={() => setIsDetailsModalOpen(false)}
                className="p-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-white border border-slate-800"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Details Content */}
            <div className="p-6 space-y-4">
              
              {/* Top Banner */}
              <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-mono text-slate-500 uppercase tracking-widest">Database Type</span>
                  <div className="mt-1">{renderDbTypeBadge(viewingDb.databaseType)}</div>
                </div>
                <div className="text-right">
                  <span className="text-[10px] font-mono text-slate-500 uppercase tracking-widest">Connection Status</span>
                  <div className="mt-1">{renderStatusBadge(viewingDb)}</div>
                </div>
              </div>

              {/* Grid Properties */}
              <div className="grid grid-cols-2 gap-3 text-xs font-mono">
                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Host</span>
                  <p className="text-slate-200 font-bold mt-0.5">{viewingDb.host}</p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Port</span>
                  <p className="text-cyan-400 font-bold mt-0.5">{viewingDb.port}</p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Database Name</span>
                  <p className="text-slate-200 font-bold mt-0.5">{viewingDb.databaseName}</p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Username</span>
                  <p className="text-slate-200 font-bold mt-0.5">{viewingDb.username}</p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Database Version</span>
                  <p className="text-amber-300 font-bold mt-0.5 truncate">{viewingDb.databaseVersion || 'Not Tested Yet'}</p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Last Response Time</span>
                  <p className="text-cyan-300 font-bold mt-0.5">
                    {viewingDb.lastResponseTimeMs !== undefined && viewingDb.lastResponseTimeMs !== null ? `${viewingDb.lastResponseTimeMs} ms` : 'N/A'}
                  </p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Last Connection Test</span>
                  <p className="text-slate-300 mt-0.5">
                    {viewingDb.lastConnectionTest ? new Date(viewingDb.lastConnectionTest).toLocaleString() : 'Never'}
                  </p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Created By</span>
                  <p className="text-slate-300 mt-0.5">{viewingDb.createdBy || 'Super Admin'}</p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Created Date</span>
                  <p className="text-slate-400 mt-0.5">
                    {viewingDb.createdAt ? new Date(viewingDb.createdAt).toLocaleString() : 'N/A'}
                  </p>
                </div>

                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
                  <span className="text-slate-500 text-[10px]">Updated Date</span>
                  <p className="text-slate-400 mt-0.5">
                    {viewingDb.updatedAt ? new Date(viewingDb.updatedAt).toLocaleString() : 'N/A'}
                  </p>
                </div>
              </div>

              {viewingDb.description && (
                <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl text-xs">
                  <span className="text-slate-500 text-[10px] font-mono">Description</span>
                  <p className="text-slate-300 mt-1">{viewingDb.description}</p>
                </div>
              )}

              {/* Password Protection Badge */}
              <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between text-xs font-mono">
                <span className="text-slate-400 flex items-center gap-2">
                  <Lock className="w-4 h-4 text-emerald-400" /> Password Encryption
                </span>
                <span className="text-emerald-400 font-bold bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                  AES-256 Secured
                </span>
              </div>

              {/* Actions Footer */}
              <div className="flex items-center justify-end gap-3 pt-2">
                {isSecurityOfficer && (
                  <button
                    onClick={() => {
                      setIsDetailsModalOpen(false);
                      handleTestConnection(viewingDb.id, viewingDb.connectionName);
                    }}
                    className="px-4 py-2 rounded-xl bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-300 font-mono text-xs border border-cyan-500/40 flex items-center gap-2"
                  >
                    <Zap className="w-3.5 h-3.5" /> Test Connection Now
                  </button>
                )}
                <button
                  onClick={() => setIsDetailsModalOpen(false)}
                  className="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 font-mono text-xs border border-slate-800"
                >
                  Close
                </button>
              </div>

            </div>
          </div>
        </div>
      )}

      {/* ── MODAL 3: DELETE CONFIRMATION ───────────────────────────────────── */}
      {isDeleteModalOpen && deletingDb && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
          <div className="w-full max-w-md glass-panel rounded-3xl border border-rose-500/30 bg-[#0c1019] shadow-2xl overflow-hidden p-6 space-y-4">
            <div className="w-12 h-12 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-center justify-center text-rose-400 mx-auto">
              <Trash2 className="w-6 h-6" />
            </div>
            
            <div className="text-center space-y-1">
              <h3 className="text-lg font-bold text-white">Delete Database Connection?</h3>
              <p className="text-xs text-slate-400">
                Are you sure you want to delete <strong className="text-slate-200">{deletingDb.connectionName}</strong> ({deletingDb.host})?
              </p>
              <p className="text-[11px] text-rose-400 font-mono pt-2">
                ⚠️ This action cannot be undone. Saved credentials and scan references will be removed.
              </p>
            </div>

            <div className="flex items-center justify-center gap-3 pt-2">
              <button
                onClick={() => setIsDeleteModalOpen(false)}
                className="px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 text-xs font-mono border border-slate-800 flex-1"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteConfirm}
                disabled={actionLoading}
                className="px-4 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs flex-1 flex items-center justify-center gap-2 shadow-lg shadow-rose-600/20"
              >
                {actionLoading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Trash2 className="w-4 h-4" />}
                Delete Connection
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL 4: TEST CONNECTION RESULT ────────────────────────────────── */}
      {isTestResultModalOpen && testResult && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fadeIn">
          <div className={`w-full max-w-md glass-panel rounded-3xl border bg-[#0c1019] shadow-2xl overflow-hidden p-6 space-y-4 ${
            testResult.success ? 'border-emerald-500/40' : 'border-rose-500/40'
          }`}>
            
            {/* Icon */}
            <div className={`w-14 h-14 rounded-2xl flex items-center justify-center mx-auto border ${
              testResult.success 
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' 
                : 'bg-rose-500/10 border-rose-500/30 text-rose-400'
            }`}>
              {testResult.success ? <CheckCircle2 className="w-8 h-8" /> : <XCircle className="w-8 h-8" />}
            </div>

            {/* Title & Message */}
            <div className="text-center space-y-1">
              <h3 className={`text-lg font-extrabold ${testResult.success ? 'text-emerald-400' : 'text-rose-400'}`}>
                {testResult.success ? 'Connection Successful' : 'Connection Failed'}
              </h3>
              <p className="text-xs text-slate-300 font-mono font-semibold">
                Target: {testResult.dbName}
              </p>
              <p className="text-xs text-slate-400 pt-1">
                {testResult.message}
              </p>
            </div>

            {/* Metadata Box */}
            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl space-y-2.5 font-mono text-xs">
              <div className="flex items-center justify-between text-slate-400">
                <span>Database Version</span>
                <span className="text-amber-300 font-semibold">{testResult.databaseVersion}</span>
              </div>
              <div className="flex items-center justify-between text-slate-400">
                <span>Response Time</span>
                <span className="text-cyan-400 font-semibold">{testResult.responseTimeMs} ms</span>
              </div>
              <div className="flex items-center justify-between text-slate-400">
                <span>Tested At</span>
                <span className="text-slate-300">{testResult.timestamp}</span>
              </div>
            </div>

            {/* Action */}
            <div className="pt-2 text-center">
              <button
                onClick={() => setIsTestResultModalOpen(false)}
                className={`w-full py-2.5 rounded-xl font-bold text-xs transition-all ${
                  testResult.success
                    ? 'bg-emerald-500 hover:bg-emerald-400 text-slate-950'
                    : 'bg-slate-800 hover:bg-slate-700 text-slate-200'
                }`}
              >
                Done
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
};
