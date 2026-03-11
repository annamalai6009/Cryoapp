import React from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { format } from 'date-fns';
import { ChartDataPoint } from '@/services/freezerService';
import { Loader2 } from 'lucide-react';

interface DataLoggerChannelChartProps {
  data: ChartDataPoint[];
  loading: boolean;
}

export const DataLoggerChannelChart: React.FC<DataLoggerChannelChartProps> = ({
  data,
  loading,
}) => {

  if (loading) {
    return (
      <div className="h-[350px] flex items-center justify-center">
        <Loader2 className="animate-spin text-blue-500" />
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="h-[350px] flex items-center justify-center text-gray-400 border border-dashed rounded-lg">
        No history data found for this range
      </div>
    );
  }

  return (
    <div className="h-[350px] w-full border rounded-lg p-4 bg-white shadow-sm">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
          <XAxis
            dataKey="timestamp"
            tickFormatter={(ts) => format(new Date(ts), 'HH:mm')}
            fontSize={11}
          />
          <YAxis domain={['auto', 'auto']} fontSize={11} unit="°C" />
          <Tooltip labelFormatter={(ts) => format(new Date(ts), 'PPpp')} />
          <Line
            type="monotone"
            dataKey="temperature"
            stroke="#3b82f6"
            strokeWidth={2}
            dot={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

