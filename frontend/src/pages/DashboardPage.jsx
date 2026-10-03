import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import { Header } from '../components/layout/Header';
import { Sidebar } from '../components/layout/Sidebar';
import { KpiCard } from '../components/dashboard/KpiCard';
import { StatusWidgetBar } from '../components/dashboard/StatusWidgetBar';
import { DashboardCharts } from '../components/dashboard/DashboardCharts';
import { ActivityTable } from '../components/dashboard/ActivityTable';
import { ScanResultsTable } from '../components/dashboard/ScanResultsTable';
import { DatabaseManagementPage } from './DatabaseManagementPage';
import SchedulerPage from './SchedulerPage';
import PiiDetectionPage from './PiiDetectionPage';
import RiskClassificationPage from './RiskClassificationPage';
import DataMaskingPage from './DataMaskingPage';
import UserManagementPage from './UserManagementPage';
import RolesPermissionsPage from './RolesPermissionsPage';
import AuditLogsPage from './AuditLogsPage';
import ReportsPage from './ReportsPage';
import { ExplainableDecisionsPage } from './ExplainableDecisionsPage';
import AccessDeniedPage from './AccessDeniedPage';
import { useAuth } from '../context/AuthContext';
import { 
  Database, Search, History, FileText, BarChart3, Users, Settings, 
  RefreshCw, Plus, Play, Download, ShieldCheck, Calendar
} from 'lucide-react';

export const DashboardPage = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const { hasPermission } = useAuth();

  // Date Filtering State
  const [dateRange, setDateRange] = useState('LAST_30_DAYS');
  const [customFrom, setCustomFrom] = useState('');
  const [customTo, setCustomTo] = useState('');

  // Dashboard state
  const [overview, setOverview] = useState(null);
  const [activities, setActivities] = useState([]);
  const [chartsData, setChartsData] = useState(null);
  const [scans, setScans] = useState([]);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const params = { range: dateRange };
      if (dateRange === 'CUSTOM' && customFrom && customTo) {
        params.dateFrom = customFrom;
        params.dateTo = customTo;
      }

      const [ovRes, actRes, chartRes, scanRes] = await Promise.all([
        API.get('/dashboard/overview', { params }).catch(() => null),
        API.get('/dashboard/activity', { params }).catch(() => null),
        API.get('/dashboard/charts', { params }).catch(() => null),
        API.get('/dashboard/scans', { params }).catch(() => null)
      ]);

      if (ovRes?.data?.success) setOverview(ovRes.data.data);
      if (actRes?.data?.success) setActivities(actRes.data.data);
      if (chartRes?.data?.success) setChartsData(chartRes.data.data);
      if (scanRes?.data?.success) setScans(scanRes.data.data);
    } catch (err) {
      console.error("Error fetching dashboard metrics:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, [dateRange, customFrom, customTo]);

  const toggleSidebar = () => setIsSidebarOpen(!isSidebarOpen);

  // Render modular view if user navigates away from main dashboard
  const renderTabContent = () => {
    switch (activeTab) {
      case 'database':
        return <DatabaseManagementPage />;

      case 'scheduler':
        return <SchedulerPage />;

      case 'pii-detection':
        return <PiiDetectionPage />;

      case 'risk-classification':
        return <RiskClassificationPage />;

      case 'data-masking':
        return <DataMaskingPage />;

      case 'decisions':
        return <ExplainableDecisionsPage />;

      case 'users':
        return <UserManagementPage />;

      case 'roles-permissions':
        return <RolesPermissionsPage />;

      case 'audit-logs':
        return <AuditLogsPage />;

      case 'reports':
        return hasPermission('REPORT_VIEW') ? <ReportsPage /> : <AccessDeniedPage message="You do not have permission to view Enterprise Reports." />;

      case 'analytics':
        return (
          <div className="p-8 text-center glass-panel rounded-3xl border border-slate-800 space-y-4">
            <BarChart3 className="w-12 h-12 text-cyan-400 mx-auto" />
            <h2 className="text-xl font-bold text-white">Advanced Database Risk Analytics</h2>
            <p className="text-slate-400 text-sm max-w-lg mx-auto">
              Real-time privacy risk exposure, cross-database compliance matrices, and query impact metrics.
            </p>
          </div>
        );

      default:
        return (
          <div className="space-y-8">
            {/* Date Range Toolbar */}
            <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-2xl bg-slate-900/60 border border-slate-800">
              <div className="flex items-center gap-2 text-xs font-mono text-slate-300">
                <Calendar className="w-4 h-4 text-cyan-400" />
                <span className="font-semibold uppercase tracking-wider">Analytics Period:</span>
              </div>

              <div className="flex flex-wrap items-center gap-2">
                {['TODAY', 'LAST_7_DAYS', 'LAST_30_DAYS', 'CUSTOM'].map((r) => (
                  <button
                    key={r}
                    onClick={() => setDateRange(r)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-mono transition-all ${
                      dateRange === r
                        ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-bold'
                        : 'bg-slate-950 text-slate-400 hover:bg-slate-800 border border-slate-800'
                    }`}
                  >
                    {r.replace('_', ' ')}
                  </button>
                ))}

                {dateRange === 'CUSTOM' && (
                  <div className="flex items-center gap-2 ml-2">
                    <input
                      type="date"
                      value={customFrom}
                      onChange={(e) => setCustomFrom(e.target.value)}
                      className="bg-slate-950 border border-slate-800 text-white text-xs font-mono px-2 py-1 rounded-lg"
                    />
                    <span className="text-slate-500 text-xs font-mono">to</span>
                    <input
                      type="date"
                      value={customTo}
                      onChange={(e) => setCustomTo(e.target.value)}
                      className="bg-slate-950 border border-slate-800 text-white text-xs font-mono px-2 py-1 rounded-lg"
                    />
                  </div>
                )}
              </div>
            </div>

            {/* Summary Status Widgets Bar */}
            <StatusWidgetBar overview={overview} />

            {/* 8 KPI Cards Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
              <KpiCard
                title="Total Databases"
                value={overview?.totalDatabases || 0}
                change="Connected DBs"
                isPositive={true}
                iconType="databases"
                subtitle="Target Systems"
                colorScheme="cyan"
              />
              <KpiCard
                title="Total Tables"
                value={overview?.totalTables || 0}
                change="Tables Analyzed"
                isPositive={true}
                iconType="tables"
                subtitle="Across Schemas"
                colorScheme="indigo"
              />
              <KpiCard
                title="Total Records"
                value={overview?.totalRecords ? `${(overview.totalRecords / 1000000).toFixed(2)}M` : '0'}
                change="Indexed Rows"
                isPositive={true}
                iconType="records"
                subtitle="DB Target Size"
                colorScheme="emerald"
              />
              <KpiCard
                title="Records Scanned"
                value={overview?.recordsScanned ? `${(overview.recordsScanned / 1000000).toFixed(2)}M` : '0'}
                change="Period Total"
                isPositive={true}
                iconType="scanned"
                subtitle="Verified Rows"
                colorScheme="teal"
              />
              <KpiCard
                title="PII Detected"
                value={overview?.piiDetected !== undefined ? overview.piiDetected : 0}
                change="Findings"
                isPositive={false}
                iconType="pii"
                subtitle="Detected Elements"
                colorScheme="amber"
              />
              <KpiCard
                title="High Risk Records"
                value={overview?.highRiskRecords !== undefined ? overview.highRiskRecords : 0}
                change="Critical + High"
                isPositive={false}
                iconType="highRisk"
                subtitle="High Exposure"
                colorScheme="rose"
              />
              <KpiCard
                title="Policy Violations"
                value={overview?.policyViolations !== undefined ? overview.policyViolations : 0}
                change="Unmasked High Risk"
                isPositive={false}
                iconType="violations"
                subtitle="Policy Health"
                colorScheme="purple"
              />
              <KpiCard
                title="Active Users"
                value={overview?.activeUsers || 0}
                change="Registered"
                isPositive={true}
                iconType="users"
                subtitle="System Accounts"
                colorScheme="blue"
              />
            </div>

            {/* 4 Interactive Recharts */}
            <DashboardCharts chartsData={chartsData} />

            {/* 2 Detailed Tables */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <ActivityTable activities={activities} />
              <ScanResultsTable scans={scans} />
            </div>
          </div>
        );
    }
  };

  return (
    <div className="min-h-screen bg-[#06080e] text-slate-100 flex overflow-hidden font-sans">
      {/* Sidebar Navigation */}
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        isOpen={isSidebarOpen} 
        setIsOpen={setIsSidebarOpen} 
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        <Header 
          activeTab={activeTab} 
          toggleSidebar={toggleSidebar} 
          onRefresh={fetchDashboardData}
          loading={loading}
        />

        <main className="flex-1 p-4 lg:p-8 space-y-8 max-w-[1600px] w-full mx-auto">
          {renderTabContent()}
        </main>
      </div>
    </div>
  );
};
