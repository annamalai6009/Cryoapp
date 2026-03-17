import React, { useCallback, useMemo, useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { format, differenceInMinutes } from 'date-fns';
import { DashboardLayout } from '@/components/DashboardLayout';
import { DataLoggerChannelChart } from '@/components/dashboard/DataLoggerChannelChart';
import { freezerService, ChartDataPoint, FreezerStatusResponse } from '@/services/freezerService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
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
const daysAgoStr = (days: number) => {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return format(d, 'yyyy-MM-dd');
};

export default function DataLoggerChannelDetails() {
  const { id, channel } = useParams<{ id: string; channel: string }>();
  const [fromDate, setFromDate] = useState(daysAgoStr(7));
  const [toDate, setToDate] = useState(todayStr());
  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [currentTemp, setCurrentTemp] = useState<number | null>(null);
  const [channelTelemetry, setChannelTelemetry] = useState<{
    timestamp: string | null;
    lowTemperature: number | null;
    setTemperature: number | null;
    highTemperature: number | null;
    highAlarm: boolean | null;
    lowAlarm: boolean | null;
  }>({
    timestamp: null,
    lowTemperature: null,
    setTemperature: null,
    highTemperature: null,
    highAlarm: null,
    lowAlarm: null,
  });
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

  const title = useMemo(() => `${channel ?? ''} Dashboard`, [channel]);

  const channelCandidates = useCallback((raw: string): string[] => {
    const s = String(raw).trim();
    if (!s) return [];

    const out: string[] = [s];

    // Accept both "1" and "CH1" styles depending on what backend stored.
    const m = s.match(/^ch\s*0*(\d+)$/i);
    if (m?.[1]) {
      out.push(m[1]); // "CH1" -> "1"
    } else if (/^\d+$/.test(s)) {
      out.push(`CH${s}`); // "1" -> "CH1"
    }

    // De-dupe while preserving order
    return Array.from(new Set(out));
  }, []);

  const normalizeChannelKey = useCallback((raw: string): string => {
    const s = String(raw).trim();
    const m = s.match(/^ch\s*0*(\d+)$/i);
    if (m?.[1]) return m[1];
    return s;
  }, []);

  const fetchLatestTemp = useCallback(async () => {
    if (!id || !channel) return;
    try {
      const status = (await freezerService.getFreezerStatus(
        id
      )) as unknown as FreezerStatusResponse & {
        channels?: Array<{
          channelNumber?: string | number | null;
          temperature?: number | string | null;
          setTemperature?: number | string | null;
          highTemperature?: number | string | null;
          lowTemperature?: number | string | null;
          highAlarm?: boolean | string | null;
          lowAlarm?: boolean | string | null;
          timestamp?: string | null;
        }>;
      };

      const channels = status?.channels ?? [];
      const target = normalizeChannelKey(channel);
      const found = channels.find((c) => {
        const k = c?.channelNumber;
        if (k == null) return false;
        return normalizeChannelKey(String(k)) === target;
      });
      const toNum = (v: unknown): number | null => {
        if (typeof v === 'number' && Number.isFinite(v)) return v;
        if (typeof v === 'string' && v.trim() !== '') {
          const n = Number(v);
          return Number.isFinite(n) ? n : null;
        }
        return null;
      };
      const toBool = (v: unknown): boolean | null => {
        if (typeof v === 'boolean') return v;
        if (typeof v === 'string') {
          const s = v.trim().toUpperCase();
          if (s === 'ON' || s === 'TRUE') return true;
          if (s === 'OFF' || s === 'FALSE') return false;
        }
        return null;
      };

      setCurrentTemp(toNum(found?.temperature));
      setChannelTelemetry({
        timestamp: found?.timestamp ?? null,
        lowTemperature: toNum(found?.lowTemperature),
        setTemperature: toNum(found?.setTemperature),
        highTemperature: toNum(found?.highTemperature),
        highAlarm: toBool(found?.highAlarm),
        lowAlarm: toBool(found?.lowAlarm),
      });
    } catch (e) {
      console.error('Latest channel temp fetch failed', e);
      // keep currentTemp as-is; chart may still load
    }
  }, [channel, id, normalizeChannelKey]);

  const loadChannelData = useCallback(async () => {
    if (!id || !channel) return;
    try {
      setLoading(true);
      const fromIso = `${fromDate}T00:00:00`;
      const toIso = `${toDate}T23:59:59`;

      const candidates = channelCandidates(channel);
      let raw: ChartDataPoint[] = [];
      for (const c of candidates) {
        // eslint-disable-next-line no-await-in-loop
        const attempt = await freezerService.getFreezerChannelChart(id, c, fromIso, toIso);
        if (attempt && Array.isArray(attempt) && attempt.length > 0) {
          raw = attempt;
          break;
        }
      }

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

      // Prefer latest temp from status; fall back to last chart point
      setCurrentTemp((prev) => prev ?? safe[safe.length - 1].temperature);
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
    fetchLatestTemp();
    loadChannelData();
  }, [fetchLatestTemp, loadChannelData]);

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
        <div className="p-10 text-center">Data logger channel not found</div>
      </DashboardLayout>
    );
  }

  const fahrenheit =
    currentTemp != null ? (currentTemp * 9) / 5 + 32 : null;

  const lastUpdate =
    channelTelemetry.timestamp ? new Date(channelTelemetry.timestamp) : null;
  const minutesSinceUpdate =
    lastUpdate ? differenceInMinutes(new Date(), lastUpdate) : Number.POSITIVE_INFINITY;
  const isOnline = Number.isFinite(minutesSinceUpdate) && minutesSinceUpdate <= 10;

  const isAlarm =
    channelTelemetry.highAlarm === true || channelTelemetry.lowAlarm === true;

  // Accent colors for gauges based on thresholds
  let cGaugeColor = '#22c55e';
  let fGaugeColor = '#3b82f6';
  if (
    currentTemp != null &&
    channelTelemetry.lowTemperature != null &&
    channelTelemetry.highTemperature != null
  ) {
    const outOfRange =
      currentTemp > channelTelemetry.highTemperature ||
      currentTemp < channelTelemetry.lowTemperature;
    if (outOfRange) {
      cGaugeColor = '#ef4444';
      fGaugeColor = '#ef4444';
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>

        {/* TOP: System status + performance chart, aligned with controller dashboard */}
        <div className="mt-4 flex flex-col xl:flex-row gap-6">
          {/* System status for this channel */}
          <Card className="flex-1 shadow-sm">
            <CardHeader className="space-y-1">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground">
                    System Status
                  </p>
                    <h2 className="text-lg font-semibold text-slate-900 mt-1">
                      {channel}
                    </h2>
                    <p className="text-[11px] text-slate-400 font-mono mt-1">
                      ID: {id}
                    </p>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <Badge className={isAlarm ? 'bg-red-500 animate-pulse' : 'bg-emerald-500'}>
                    {currentTemp != null ? (isAlarm ? 'CRITICAL' : 'NORMAL') : 'NO DATA'}
                  </Badge>
                  <Badge
                    variant="outline"
                    className={
                      isOnline
                        ? 'border-emerald-500 text-emerald-600'
                        : 'border-slate-300 text-slate-500'
                    }
                  >
                    {isOnline ? 'ONLINE' : 'OFFLINE'}
                  </Badge>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4 items-end">
                <div>
                    <div className="text-4xl font-bold text-emerald-600">
                      {currentTemp != null ? `${currentTemp.toFixed(1)}°` : '--'}
                    </div>
                    <p className="text-xs text-muted-foreground uppercase tracking-[0.22em]">
                      C Units
                    </p>
                </div>
                <div className="text-right">
                    <div className="text-3xl font-bold text-blue-600">
                      {fahrenheit != null ? `${fahrenheit.toFixed(1)}°` : '--'}
                    </div>
                    <p className="text-xs text-muted-foreground uppercase tracking-[0.22em]">
                      F Units
                    </p>
                </div>
              </div>

              <div className="mt-4 grid grid-cols-3 gap-2 text-center text-xs">
                <div className="rounded-xl border border-emerald-100 bg-emerald-50/60 py-3">
                  <p className="text-[10px] text-emerald-500 font-semibold tracking-wide">
                    MIN
                  </p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">
                    {chartStats.min != null ? `${chartStats.min.toFixed(1)}°` : '--'}
                  </p>
                </div>
                <div className="rounded-xl border border-sky-100 bg-sky-50/60 py-3">
                  <p className="text-[10px] text-sky-500 font-semibold tracking-wide">
                    AVG
                  </p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">
                    {chartStats.avg != null ? `${chartStats.avg.toFixed(1)}°` : '--'}
                  </p>
                </div>
                <div className="rounded-xl border border-rose-100 bg-rose-50/60 py-3">
                  <p className="text-[10px] text-rose-500 font-semibold tracking-wide">
                    MAX
                  </p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">
                    {chartStats.max != null ? `${chartStats.max.toFixed(1)}°` : '--'}
                  </p>
                </div>
              </div>

              <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
                <TemperatureGauge
                  label="Celsius"
                  value={currentTemp}
                  accentColor={cGaugeColor}
                  min={channelTelemetry.lowTemperature}
                  max={channelTelemetry.highTemperature}
                />
                <TemperatureGauge
                  label="Fahrenheit"
                  value={fahrenheit}
                  accentColor={fGaugeColor}
                  min={
                    channelTelemetry.lowTemperature != null
                      ? (channelTelemetry.lowTemperature * 9) / 5 + 32
                      : null
                  }
                  max={
                    channelTelemetry.highTemperature != null
                      ? (channelTelemetry.highTemperature * 9) / 5 + 32
                      : null
                  }
                />
              </div>

              <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-3">
                <div className="rounded-2xl bg-white shadow-sm px-4 py-3 border border-emerald-50">
                  <p className="text-[11px] font-semibold text-slate-500 tracking-[0.08em]">
                    lowTemp
                  </p>
                  <p className="mt-2 text-lg font-semibold text-slate-900">
                    {channelTelemetry.lowTemperature != null
                      ? `${channelTelemetry.lowTemperature.toFixed(1)}°C`
                      : '--'}
                  </p>
                </div>
                <div className="rounded-2xl bg-white shadow-sm px-4 py-3 border border-sky-50">
                  <p className="text-[11px] font-semibold text-slate-500 tracking-[0.08em]">
                    setTemp
                  </p>
                  <p className="mt-2 text-lg font-semibold text-slate-900">
                    {channelTelemetry.setTemperature != null
                      ? `${channelTelemetry.setTemperature.toFixed(1)}°C`
                      : '--'}
                  </p>
                </div>
                <div className="rounded-2xl bg-white shadow-sm px-4 py-3 border border-rose-50">
                  <p className="text-[11px] font-semibold text-slate-500 tracking-[0.08em]">
                    highTemp
                  </p>
                  <p className="mt-2 text-lg font-semibold text-slate-900">
                    {channelTelemetry.highTemperature != null
                      ? `${channelTelemetry.highTemperature.toFixed(1)}°C`
                      : '--'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Performance Trends card */}
          <Card className="flex-[2] shadow-sm">
            <CardHeader className="flex flex-row items-start justify-between gap-3">
              <div>
                <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground">
                  Performance Trends
                </p>
                <CardTitle className="text-base mt-1">
                  Channel temperature over time
                </CardTitle>
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
            </CardHeader>
            <CardContent className="space-y-4">
              <DataLoggerChannelChart data={chartData} loading={loading} />
            </CardContent>
          </Card>
        </div>

        {/* LOWER: Monitoring (alarm matrix like controller) */}
        <Tabs defaultValue="monitoring" className="w-full mt-4">
          <TabsList>
            <TabsTrigger value="monitoring">Monitoring</TabsTrigger>
          </TabsList>

          <TabsContent value="monitoring" className="mt-4 space-y-6">
            <Card className="shadow-sm">
              <CardHeader>
                <CardTitle>Alarm Matrix</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                  <TelemetryCard
                    label="highAlarm"
                    value={
                      channelTelemetry.highAlarm == null
                        ? '--'
                        : channelTelemetry.highAlarm
                        ? 'ON'
                        : 'OFF'
                    }
                    isAlarm={channelTelemetry.highAlarm === true}
                  />
                  <TelemetryCard
                    label="lowAlarm"
                    value={
                      channelTelemetry.lowAlarm == null
                        ? '--'
                        : channelTelemetry.lowAlarm
                        ? 'ON'
                        : 'OFF'
                    }
                    isAlarm={channelTelemetry.lowAlarm === true}
                  />
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
}

type TemperatureGaugeProps = {
  label: string;
  value: number | null;
  accentColor: string;
  /** Optional dynamic range. When provided, gauge fill is based on [min, max] instead of a fixed range. */
  min?: number | null;
  max?: number | null;
};

const TemperatureGauge = ({ label, value, accentColor, min, max }: TemperatureGaugeProps) => {
  // Match controller defaults for Celsius, but use a wider default range for Fahrenheit
  // so values like 68°F don't get clamped to 50.
  const isFahrenheit = label.trim().toLowerCase() === 'fahrenheit';
  const defaultMin = isFahrenheit ? -200 : -100;
  const defaultMax = isFahrenheit ? 250 : 50;

  const rangeMin =
    typeof min === 'number' && typeof max === 'number' && min < max ? min : defaultMin;
  const rangeMax =
    typeof min === 'number' && typeof max === 'number' && min < max ? max : defaultMax;

  const clamped =
    typeof value === 'number' ? Math.min(rangeMax, Math.max(rangeMin, value)) : null;

  const percent = clamped != null ? ((clamped - rangeMin) / (rangeMax - rangeMin)) * 100 : 0;

  const gradient =
    clamped != null
      ? `conic-gradient(${accentColor} 0 ${percent}%, #e5e7eb ${percent}% 100%)`
      : 'conic-gradient(#e5e7eb 0, #e5e7eb 100%)';

  return (
    <div className="flex items-center justify-center">
      <div
        className="w-20 h-20 rounded-full flex items-center justify-center"
        style={{ backgroundImage: gradient }}
      >
        <div className="w-14 h-14 rounded-full bg-white flex items-center justify-center">
          <span className="text-lg font-semibold text-slate-900">
            {clamped != null ? clamped.toFixed(1) : '--'}
          </span>
        </div>
      </div>
    </div>
  );
};

type TelemetryCardProps = {
  label: string;
  value: string;
  isAlarm?: boolean;
};

const TelemetryCard = ({ label, value, isAlarm }: TelemetryCardProps) => (
  <div
    className={`rounded-2xl px-4 py-3 shadow-sm border ${
      isAlarm ? 'border-red-200 bg-red-50/80' : 'border-slate-100 bg-white/70'
    }`}
  >
    <p
      className={`text-[11px] font-semibold tracking-[0.04em] ${
        isAlarm ? 'text-red-600' : 'text-slate-500'
      }`}
    >
      {label}
    </p>
    <p className={`mt-1 text-sm font-semibold ${isAlarm ? 'text-red-700' : 'text-slate-900'}`}>
      {value}
    </p>
  </div>
);

