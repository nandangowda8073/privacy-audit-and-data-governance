import React from 'react';
import { 
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend
} from 'recharts';

const RISK_COLORS = {
  LOW: '#10b981',       // emerald-500
  MEDIUM: '#eab308',    // yellow-500
  HIGH: '#f59e0b',      // amber-500
  CRITICAL: '#f43f5e'   // rose-500
};

const RiskDistributionChart = ({ summary }) => {
  const data = [
    { name: 'CRITICAL', value: summary?.critical || 0 },
    { name: 'HIGH', value: summary?.high || 0 },
    { name: 'MEDIUM', value: summary?.medium || 0 },
    { name: 'LOW', value: summary?.low || 0 },
  ].filter(d => d.value > 0);

  if (data.length === 0) {
    return (
      <div className="h-64 flex items-center justify-center text-slate-500 text-sm">
        No risk data available
      </div>
    );
  }

  return (
    <div className="h-64 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={80}
            paddingAngle={5}
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={RISK_COLORS[entry.name]} stroke="rgba(0,0,0,0)" />
            ))}
          </Pie>
          <Tooltip 
            contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.5rem', color: '#f8fafc' }}
            itemStyle={{ color: '#f8fafc' }}
          />
          <Legend wrapperStyle={{ fontSize: '12px', color: '#94a3b8' }} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};

export default RiskDistributionChart;
