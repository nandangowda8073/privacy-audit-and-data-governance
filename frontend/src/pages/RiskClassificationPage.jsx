import React, { useState, useEffect } from 'react';
import { ShieldAlert, Activity } from 'lucide-react';
import { riskApi } from '../api/riskApi';
import RiskKpiCards from '../components/risk/RiskKpiCards';
import RiskDistributionChart from '../components/risk/RiskDistributionChart';
import RiskAssessmentTable from '../components/risk/RiskAssessmentTable';

const RiskClassificationPage = () => {
  const [summary, setSummary] = useState(null);
  const [assessments, setAssessments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchRiskData = async () => {
    try {
      const [summaryRes, assessmentsRes] = await Promise.all([
        riskApi.getSummary(),
        riskApi.getAll()
      ]);
      setSummary(summaryRes.data.data);
      setAssessments(assessmentsRes.data.data);
    } catch (err) {
      console.error('Failed to fetch risk data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRiskData();
  }, []);

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
            <ShieldAlert className="w-8 h-8 text-rose-500" />
            Risk Classification
          </h1>
          <p className="text-slate-400 mt-2">Monitor, analyze, and review assigned risk levels for detected PII elements.</p>
        </div>
      </div>

      <RiskKpiCards summary={summary} />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="glass-panel p-5 rounded-2xl border border-slate-800 lg:col-span-1">
          <div className="border-b border-slate-800 pb-3 mb-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <Activity className="w-5 h-5 text-cyan-400" />
              Risk Distribution
            </h2>
          </div>
          <RiskDistributionChart summary={summary} />
        </div>
        
        <div className="lg:col-span-2">
          <RiskAssessmentTable assessments={assessments} />
        </div>
      </div>
    </div>
  );
};

export default RiskClassificationPage;
