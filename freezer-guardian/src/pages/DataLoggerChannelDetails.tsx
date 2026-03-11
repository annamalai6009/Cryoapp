import React, { useCallback, useMemo, useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { format } from 'date-fns';
import { DashboardLayout } from '@/components/DashboardLayout';
import { DataLoggerChannelChart } from '@/components/dashboard/DataLoggerChannelChart';
import { freezerService, ChartDataPoint } from '@/services/freezerService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Download, FileText, Loader2 } from 'lucide-react';
import { exportService } from '@/services/exportService';
import toast from 'react-hot-toast';

const todayStr = () => format(new Date(), 'yyyy-MM-dd');

export default function DataLoggerChannelDetails() {
  const { id, channel } = useParams<{ id: string; channel: string }>();
  const [fromDate, setFromDate] = useState(todayStr());
  const [toDate, setToDate] = useState(todayStr());
  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [currentTemp, setCurrentTemp] = useState<number | null>(null);
  const [chartStats, setChartStats] = useState<{
    min: number | null;
    max: number | null;
    avg: number | null;
  }>({
    min: null,
    max: null,
    avg: null,
  });
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [exportFormat, setExportFormat] = useState<'pdf' | 'csv'>('pdf');
  const [exporting, setExporting] = useState<'pdf' | 'csv' | null>(null);

  const title = useMemo(() => `Channel ${channel ?? ''} Dashboard`, [channel]);

  const loadChannelData = useCallback(async () => {
    if (!id || !channel) return;
    try {
      setLoading(true);
      const fromIso = `${fromDate}T00:00:00`;
      const toIso = `${toDate}T23:59:59`;

      const raw = await freezerService.getFreezerChannelChart(
        id,
        channel,
        fromIso,
        toIso
      );

      if (!raw || !Array.isArray(raw) || raw.length === 0) {
        setChartData([]);
        setCurrentTemp(null);
        setChartStats({ min: null, max: null, avg: null });
        return;
      }

      const safe = raw
        .filter((d) => d.temperature != null)
        .map((d) => ({
          ...d,
          temperature: Number(d.temperature),
        }));

      setChartData(safe);

      if (!safe.length) {
        setCurrentTemp(null);
        setChartStats({ min: null, max: null, avg: null });
        return;
      }

      const temps = safe.map((d) => d.temperature);
      const min = Math.min(...temps);
      const max = Math.max(...temps);
      const avg = temps.reduce((sum, t) => sum + t, 0) / temps.length;

      setCurrentTemp(safe[safe.length - 1].temperature);
      setChartStats({ min, max, avg });
    } catch (e) {
      console.error('Channel data load failed', e);
      setChartData([]);
      setCurrentTemp(null);
      setChartStats({ min: null, max: null, avg: null });
    } finally {
      setLoading(false);
    }
  }, [id, channel, fromDate, toDate]);

  useEffect(() => {
    loadChannelData();
  }, [loadChannelData]);

  const handleExport = useCallback(
    async (type: 'pdf' | 'csv') => {
      if (!id || !channel) return;
      setExporting(type);
      try {
        const fromIso = `${fromDate}T00:00:00`;
        const toIso = `${toDate}T23:59:59`;

        const blob =
          type === 'pdf'
            ? await exportService.exportToPdf(id, fromIso, toIso)
            : await exportService.exportToCsv(id, fromIso, toIso);

        const safeChannel = String(channel).replace(/\s+/g, '');
        exportService.downloadBlob(
          blob,
          `datalogger-${id}-ch${safeChannel}-${fromDate}.${type}`
        );
        toast.success(`${type.toUpperCase()} Downloaded`);
      } catch {
        toast.error('Export failed. Try a smaller date range.');
      } finally {
        setExporting(null);
      }
    },
    [channel, fromDate, id, toDate]
  );

  if (!id || !channel) {
    return (
      <DashboardLayout>
        <div className="max-w-5xl mx-auto py-8">
          <div className="mt-8 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            Missing data logger or channel information in the URL.
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="max-w-6xl mx-auto py-6 space-y-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h1 className="text-xl font-semibold text-gray-900">
              {title}
            </h1>
            <p className="text-sm text-gray-500">
              Data Logger ID: <span className="font-mono">{id}</span>
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* System status for this channel */}
          <div className="rounded-lg border bg-white px-4 py-3 shadow-sm">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs uppercase tracking-[0.18em] text-gray-500">
                  System Status
                </p>
                <h2 className="text-lg font-semibold text-gray-900 mt-1">
                  Channel {channel}
                </h2>
                <p className="text-[11px] text-gray-400 font-mono mt-1">
                  ID: {id}
                </p>
              </div>
              <span className="inline-flex items-center rounded-full bg-emerald-500 px-3 py-1 text-xs font-semibold text-white">
                NORMAL
              </span>
            </div>

            <div className="mt-4 text-3xl font-bold text-emerald-600">
              {currentTemp != null ? `${currentTemp.toFixed(1)}°` : '--'}
            </div>
            <p className="text-xs text-gray-400 uppercase tracking-[0.22em] mt-1">
              C Units
            </p>

            <div className="mt-4 grid grid-cols-3 gap-2 text-center text-xs">
              <div className="rounded-xl border border-emerald-100 bg-emerald-50/60 py-3">
                <p className="text-[10px] text-emerald-500 font-semibold tracking-wide">
                  MIN
                </p>
                <p className="mt-1 text-sm font-semibold text-gray-900">
                  {chartStats.min != null ? `${chartStats.min.toFixed(1)}°` : '--'}
                </p>
              </div>
              <div className="rounded-xl border border-sky-100 bg-sky-50/60 py-3">
                <p className="text-[10px] text-sky-500 font-semibold tracking-wide">
                  AVG
                </p>
                <p className="mt-1 text-sm font-semibold text-gray-900">
                  {chartStats.avg != null ? `${chartStats.avg.toFixed(1)}°` : '--'}
                </p>
              </div>
              <div className="rounded-xl border border-rose-100 bg-rose-50/60 py-3">
                <p className="text-[10px] text-rose-500 font-semibold tracking-wide">
                  MAX
                </p>
                <p className="mt-1 text-sm font-semibold text-gray-900">
                  {chartStats.max != null ? `${chartStats.max.toFixed(1)}°` : '--'}
                </p>
              </div>
            </div>
          </div>

          {/* Empty slot reserved for future content */}
        </div>

        <div className="rounded-lg border bg-white px-4 py-4 shadow-sm">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h2 className="text-sm font-semibold text-gray-900">
                Performance Trends
              </h2>
              <p className="text-xs text-gray-500">
                Channel-specific temperature readings over the selected range
              </p>
            </div>
            <Dialog open={isExportOpen} onOpenChange={setIsExportOpen}>
              <DialogTrigger asChild>
                <Button
                  variant="outline"
                  size="icon"
                  className="rounded-full shadow-sm"
                  disabled={!!exporting}
                >
                  {exporting ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <Download className="w-4 h-4" />
                  )}
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-sm">
                <DialogHeader>
                  <DialogTitle>Export Data</DialogTitle>
                  <DialogDescription>
                    Choose format and date range to download a report for this
                    channel.
                  </DialogDescription>
                </DialogHeader>
                <div className="space-y-5 pt-2">
                  <div>
                    <p className="text-xs font-semibold tracking-[0.16em] text-slate-500 uppercase">
                      Format
                    </p>
                    <div className="mt-3 grid grid-cols-2 gap-2">
                      <Button
                        type="button"
                        variant={exportFormat === 'pdf' ? 'default' : 'outline'}
                        className={
                          exportFormat === 'pdf'
                            ? 'bg-slate-900 text-white border-slate-900'
                            : 'bg-white'
                        }
                        onClick={() => setExportFormat('pdf')}
                      >
                        <FileText className="w-4 h-4 mr-2" />
                        PDF Report
                      </Button>
                      <Button
                        type="button"
                        variant={exportFormat === 'csv' ? 'default' : 'outline'}
                        className={
                          exportFormat === 'csv'
                            ? 'bg-slate-900 text-white border-slate-900'
                            : 'bg-white'
                        }
                        onClick={() => setExportFormat('csv')}
                      >
                        <Download className="w-4 h-4 mr-2" />
                        CSV Table
                      </Button>
                    </div>
                  </div>

                  <div>
                    <p className="text-xs font-semibold tracking-[0.16em] text-slate-500 uppercase">
                      Date Range
                    </p>
                    <div className="mt-3 flex flex-col gap-2">
                      <Input
                        type="date"
                        value={fromDate}
                        onChange={(e) => setFromDate(e.target.value)}
                        className="h-9 text-sm"
                      />
                      <Input
                        type="date"
                        value={toDate}
                        onChange={(e) => setToDate(e.target.value)}
                        className="h-9 text-sm"
                      />
                    </div>
                  </div>

                  <Button
                    type="button"
                    className="w-full bg-slate-900 hover:bg-slate-900/90"
                    disabled={!!exporting}
                    onClick={async () => {
                      await handleExport(exportFormat);
                      setIsExportOpen(false);
                    }}
                  >
                    {exporting ? (
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    ) : null}
                    Download Archive
                  </Button>
                </div>
              </DialogContent>
            </Dialog>
          </div>

          <DataLoggerChannelChart data={chartData} loading={loading} />
        </div>
      </div>
    </DashboardLayout>
  );
}

