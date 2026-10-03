import React, { useState, useEffect } from 'react';
import axiosInstance from '../api/axios';
import SchedulerStatusCard from '../components/scheduler/SchedulerStatusCard';
import SchedulerConfigForm from '../components/scheduler/SchedulerConfigForm';
import ScanHistoryTable from '../components/scheduler/ScanHistoryTable';
import { CalendarClock } from 'lucide-react';

const SchedulerPage = () => {
  const [status, setStatus] = useState(null);
  const [config, setConfig] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [isTriggering, setIsTriggering] = useState(false);

  const fetchData = async () => {
    try {
      const [statusRes, configRes, historyRes] = await Promise.all([
        axiosInstance.get('/api/scheduler/status'),
        axiosInstance.get('/api/scheduler/config'),
        axiosInstance.get('/api/scheduler/history')
      ]);
      
      setStatus(statusRes.data.data);
      setConfig(configRes.data.data);
      setHistory(historyRes.data.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching scheduler data:', err);
      setError('Failed to load scheduler data. Ensure you have the right permissions.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // Poll for updates if a scan is running
    const interval = setInterval(() => {
      if (status?.currentStatus === 'RUNNING' || isTriggering) {
        fetchData();
      }
    }, 5000);
    return () => clearInterval(interval);
  }, [status?.currentStatus, isTriggering]);

  const handleStart = async () => {
    try {
      await axiosInstance.post('/api/scheduler/start');
      fetchData();
    } catch (err) {
      console.error(err);
      setError('Failed to start scheduler.');
    }
  };

  const handleStop = async () => {
    try {
      await axiosInstance.post('/api/scheduler/stop');
      fetchData();
    } catch (err) {
      console.error(err);
      setError('Failed to stop scheduler.');
    }
  };

  const handleTriggerScan = async () => {
    setIsTriggering(true);
    try {
      await axiosInstance.post('/api/scheduler/run');
      await fetchData();
    } catch (err) {
      console.error(err);
      setError('Failed to trigger scan.');
    } finally {
      setIsTriggering(false);
    }
  };

  const handleSaveConfig = async (newConfig) => {
    setIsSaving(true);
    try {
      await axiosInstance.put('/api/scheduler/config', newConfig);
      await fetchData();
    } catch (err) {
      console.error(err);
      setError('Failed to save configuration.');
    } finally {
      setIsSaving(false);
    }
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
            <CalendarClock className="w-8 h-8 text-cyan-400" />
            Scheduled Scan Engine
          </h1>
          <p className="text-slate-400 mt-2">Configure automated database scans and review execution history.</p>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-sm">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <SchedulerStatusCard 
          status={status} 
          onStart={handleStart} 
          onStop={handleStop} 
          onTriggerScan={handleTriggerScan}
          isTriggering={isTriggering}
        />
        <SchedulerConfigForm 
          config={config} 
          onSave={handleSaveConfig} 
          isSaving={isSaving}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ScanHistoryTable history={history} />
      </div>
    </div>
  );
};

export default SchedulerPage;
