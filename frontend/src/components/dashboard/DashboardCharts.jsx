import React from 'react';
import { 
  ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, AreaChart, Area 
} from 'recharts';
import { PieChart as PieIcon, BarChart2, TrendingUp, Database } from 'lucide-react';

export const DashboardCharts = ({ chartsData }) => {
  const piiCategories = chartsData?.piiByCategory || [
    { category: 'Aadhaar', count: 48500, color: '#38bdf8' },
    { category: 'PAN', count: 32400, color: '#f43f5e' },
    { category: 'Email', count: 38100, color: '#10b981' },
    { category: 'Phone', count: 18200, color: '#f59e0b' },
    { category: 'Bank Account', count: 11000, color: '#a855f7' }
  ];

  const riskData = chartsData?.riskDistribution || [
    { level: 'Low', count: 84500, color: '#10b981' },
    { level: 'Medium', count: 51250, color: '#f59e0b' },
    { level: 'High', count: 10450, color: '#f97316' },
    { level: 'Critical', count: 2000, color: '#f43f5e' }
  ];

  const dailyTrend = chartsData?.dailyScanTrend || [
    { date: '27-Jul', scans: 12, piiFound: 14200 },
    { date: '28-Jul', scans: 15, piiFound: 18900 },
    { date: '29-Jul', scans: 9, piiFound: 9800 },
    { date: '30-Jul', scans: 18, piiFound: 24100 },
    { date: '31-Jul', scans: 22, piiFound: 28500 },
    { date: '01-Aug', scans: 16, piiFound: 19400 },
    { date: '02-Aug', scans: 25, piiFound: 33300 }
  ];

  const dbStatus = chartsData?.databaseScanStatus || [
    { database: 'Customer_DB', scannedPercentage: 100, piiCount: 48200 },
    { database: 'Payment_Vault', scannedPercentage: 85, piiCount: 32100 },
    { database: 'HR_Master', scannedPercentage: 100, piiCount: 14200 },
    { database: 'Analytics_WH', scannedPercentage: 40, piiCount: 53700 },
    { database: 'Legacy_Users', scannedPercentage: 100, piiCount: 8000 }
  ];

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="glass-panel p-3 rounded-xl border border-slate-700 bg-slate-950/95 text-xs font-mono shadow-2xl">
          <p className="text-cyan-400 font-bold mb-1">{label || payload[0].name}</p>
          {payload.map((entry, index) => (
            <p key={index} style={{ color: entry.color || entry.fill }}>
              {entry.name}: <span className="font-bold text-white">{entry.value?.toLocaleString()}</span>
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      
      {/* Chart 1: PII by Category (Donut Chart) */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <PieIcon className="w-4 h-4 text-cyan-400" />
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">PII by Category</h3>
          </div>
          <span className="text-[10px] font-mono text-slate-500">Distribution breakdown</span>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={piiCategories}
                dataKey="count"
                nameKey="category"
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={95}
                paddingAngle={4}
              >
                {piiCategories.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
              <Legend 
                verticalAlign="bottom" 
                height={36} 
                formatter={(value) => <span className="text-xs font-mono text-slate-300">{value}</span>}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Chart 2: Risk Distribution (Bar Chart) */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <BarChart2 className="w-4 h-4 text-rose-400" />
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">Risk Level Distribution</h3>
          </div>
          <span className="text-[10px] font-mono text-slate-500">Classification severity</span>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={riskData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" opacity={0.6} />
              <XAxis dataKey="level" stroke="#64748b" tick={{ fontSize: 11, fontFamily: 'JetBrains Mono' }} />
              <YAxis stroke="#64748b" tick={{ fontSize: 11, fontFamily: 'JetBrains Mono' }} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="count" name="Records" radius={[6, 6, 0, 0]}>
                {riskData.map((entry, index) => (
                  <Cell key={`cell-risk-${index}`} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Chart 3: Daily Scan Trend (Area Chart) */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <TrendingUp className="w-4 h-4 text-emerald-400" />
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">Daily Scan & PII Discovery Trend</h3>
          </div>
          <span className="text-[10px] font-mono text-slate-500">7-Day Volume</span>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={dailyTrend} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="colorPii" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#38bdf8" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#38bdf8" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" opacity={0.6} />
              <XAxis dataKey="date" stroke="#64748b" tick={{ fontSize: 11, fontFamily: 'JetBrains Mono' }} />
              <YAxis stroke="#64748b" tick={{ fontSize: 11, fontFamily: 'JetBrains Mono' }} />
              <Tooltip content={<CustomTooltip />} />
              <Area type="monotone" dataKey="piiFound" name="PII Detected" stroke="#38bdf8" fillOpacity={1} fill="url(#colorPii)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Chart 4: Database Scan Status (Horizontal Bar Chart) */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Database className="w-4 h-4 text-amber-400" />
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">Database Scan Progress & PII Density</h3>
          </div>
          <span className="text-[10px] font-mono text-slate-500">% Scanned</span>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart layout="vertical" data={dbStatus} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" opacity={0.6} />
              <XAxis type="number" domain={[0, 100]} stroke="#64748b" tick={{ fontSize: 11, fontFamily: 'JetBrains Mono' }} />
              <YAxis dataKey="database" type="category" stroke="#64748b" tick={{ fontSize: 11, fontFamily: 'JetBrains Mono' }} width={95} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="scannedPercentage" name="% Completion" fill="#0ea5e9" radius={[0, 6, 6, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

    </div>
  );
};
