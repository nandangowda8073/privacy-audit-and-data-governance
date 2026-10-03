import React, { useState, useEffect } from 'react';
import { ShieldAlert } from 'lucide-react';
import { piiApi } from '../api/piiApi';
import PiiKpiCards from '../components/pii/PiiKpiCards';
import PiiCharts from '../components/pii/PiiCharts';
import DetectionPanel from '../components/pii/DetectionPanel';
import PiiEntityTable from '../components/pii/PiiEntityTable';
import CustomPatternManager from '../components/pii/CustomPatternManager';

const PiiDetectionPage = () => {
  const [stats, setStats] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDashboardData = async () => {
    try {
      const [statsRes, historyRes] = await Promise.all([
        piiApi.getStats(),
        piiApi.getHistory()
      ]);
      setStats(statsRes.data.data);
      setHistory(historyRes.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleDetectionResult = (newResult) => {
    // Refresh stats and history after a new scan
    fetchDashboardData();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="w-8 h-8 border-4 border-cyan-500/30 border-t-cyan-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight flex items-center gap-3">
            <ShieldAlert className="w-8 h-8 text-cyan-400" />
            Enterprise PII Detection
          </h1>
          <p className="text-slate-400 mt-2">Discover, classify, and manage sensitive information across data sources.</p>
        </div>
      </div>

      <PiiKpiCards stats={stats} />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <PiiCharts data={stats} />
        </div>
        <div>
          <DetectionPanel onResult={handleDetectionResult} />
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <PiiEntityTable history={history} />
        <CustomPatternManager />
      </div>
    </div>
  );
};

export default PiiDetectionPage;
