import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { DashboardLayout } from '@/components/DashboardLayout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Thermometer,
  DoorOpen,
  Zap,
  Save,
  Loader2,
  Download,
  FileText,
  Calendar
} from 'lucide-react';
import {
  freezerService,
  FreezerStatusResponse,
  FreezerConfigResponse,
  ChartDataPoint
} from '@/services/freezerService';
import { exportService } from '@/services/exportService';
import { FreezerChart } from '@/components/dashboard/FreezerChart';
import { format, subDays, differenceInMinutes } from 'date-fns';
import toast from 'react-hot-toast';

const FreezerDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [status, setStatus] = useState<FreezerStatusResponse | null>(null);
  const [config, setConfig] = useState<FreezerConfigResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [exporting, setExporting] = useState<'csv' | 'pdf' | null>(null);
  const [chartStats, setChartStats] = useState<{
    min: number | null;
    max: number | null;
    avg: number | null;
  }>({ min: null, max: null, avg: null });

  const [minThreshold, setMinThreshold] = useState<number>(0);
  const [maxThreshold, setMaxThreshold] = useState<number>(0);

  const [dateRange, setDateRange] = useState({
    from: format(subDays(new Date(), 1), 'yyyy-MM-dd'),
    to: format(new Date(), 'yyyy-MM-dd'),
  });

  const [isExportOpen, setIsExportOpen] = useState(false);
  const [exportFormat, setExportFormat] = useState<'pdf' | 'csv'>('pdf');

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      const [statusData, configData] = await Promise.all([
        freezerService.getFreezerStatus(id),
        freezerService.getFreezerConfig(id)
      ]);
      setStatus(statusData);
      if (configData) {
        setConfig(configData);
        setMinThreshold(configData.minThreshold);
        setMaxThreshold(configData.maxThreshold);
      }
    } catch (error) {
      console.error("Fetch failed", error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  // Load basic stats for the "controller" view using the same chart API
  const fetchChartStats = useCallback(async () => {
    if (!id) return;
    try {
      const fromIso = `${dateRange.from}T00:00:00`;
      const toIso = `${dateRange.to}T23:59:59`;
      const data: ChartDataPoint[] = await freezerService.getFreezerChart(id, fromIso, toIso);
      if (!data || data.length === 0) {
        setChartStats({ min: null, max: null, avg: null });
        return;
      }
      const temps = data.map(d => d.temperature).filter(t => t != null);
      if (!temps.length) {
        setChartStats({ min: null, max: null, avg: null });
        return;
      }
      const min = Math.min(...temps);
      const max = Math.max(...temps);
      const avg = temps.reduce((sum, t) => sum + t, 0) / temps.length;
      setChartStats({ min, max, avg });
    } catch {
      setChartStats({ min: null, max: null, avg: null });
    }
  }, [id, dateRange.from, dateRange.to]);

  useEffect(() => { fetchData(); }, [fetchData]);
  useEffect(() => { fetchChartStats(); }, [fetchChartStats]);

  const handleSaveSettings = async () => {
    if (!id) return;
    setSaving(true);
    try {
      await freezerService.updateSettings(id, { minThreshold, maxThreshold });
      toast.success("Settings saved!");
    } catch { toast.error("Save failed"); } 
    finally { setSaving(false); }
  };

  const handleExport = async (type: 'csv' | 'pdf') => {
    if (!id) return;
    setExporting(type);
    try {
      const blob = type === 'pdf' 
        ? await exportService.exportToPdf(id, dateRange.from, dateRange.to)
        : await exportService.exportToCsv(id, dateRange.from, dateRange.to);
      
      exportService.downloadBlob(blob, `freezer-${id}-${dateRange.from}.${type}`);
      toast.success(`${type.toUpperCase()} Downloaded`);
    } catch { 
      toast.error("Export failed. Try a smaller date range."); 
    } finally { 
      setExporting(null); 
    }
  };

  if (!id || id === 'unknown-id') {
    return (
      <DashboardLayout>
        <div className="p-10 text-center">Freezer not found</div>
      </DashboardLayout>
    );
  }

  if (loading)
    return (
      <DashboardLayout>
        <div className="flex h-screen items-center justify-center">
          <Loader2 className="animate-spin" />
        </div>
      </DashboardLayout>
    );

  if (!status)
    return (
      <DashboardLayout>
        <div className="p-10 text-center">Freezer not found</div>
      </DashboardLayout>
    );

  // Online/offline based on last reading timestamp (10-minute window)
  const lastUpdate =
    status.timestamp ? new Date(status.timestamp) : null;
  const minutesSinceUpdate =
    lastUpdate ? differenceInMinutes(new Date(), lastUpdate) : Number.POSITIVE_INFINITY;
  const isOnline = Number.isFinite(minutesSinceUpdate) && minutesSinceUpdate <= 10;

  const fahrenheit =
    status.temperature != null
      ? (status.temperature * 9) / 5 + 32
      : null;

  const lowThresholdBackend = status.lowTemp ?? null;
  const highThresholdBackend = status.highTemp ?? null;
  const setTempBackend = status.setTemp ?? null;

  const lowThreshold =
    typeof lowThresholdBackend === 'number'
      ? lowThresholdBackend
      : typeof config?.minThreshold === 'number'
      ? config.minThreshold
      : null;

  const highThreshold =
    typeof highThresholdBackend === 'number'
      ? highThresholdBackend
      : typeof config?.maxThreshold === 'number'
      ? config.maxThreshold
      : null;

  const setTemp =
    setTempBackend != null
      ? setTempBackend
      : lowThreshold != null && highThreshold != null
      ? (lowThreshold + highThreshold) / 2
      : null;

  // Accent colors for gauges based on thresholds
  const temperatureCAccessor = status.temperature ?? null;
  let cGaugeColor = '#22c55e'; // default green for °C
  let fGaugeColor = '#3b82f6'; // default blue for °F

  if (temperatureCAccessor != null && lowThreshold != null && highThreshold != null) {
    const outOfRange =
      temperatureCAccessor > highThreshold ||
      temperatureCAccessor < lowThreshold;

    if (outOfRange) {
      // Both gauges red when out of configured range
      cGaugeColor = '#ef4444';
      fGaugeColor = '#ef4444';
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-slate-900">Controller Dashboard</h1>

        {/* TOP: System status + performance chart */}
        <div className="mt-4 flex flex-col xl:flex-row gap-6">
          {/* System status card */}
          <Card className="flex-1 shadow-sm">
            <CardHeader className="space-y-1">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground">System Status</p>
                  <h2 className="text-lg font-semibold text-slate-900 mt-1">
                    {config?.name || id}
                  </h2>
                  <p className="text-[11px] text-slate-400 font-mono mt-1">
                    ID: {id}
                  </p>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <Badge className={status.isRedAlert ? "bg-red-500 animate-pulse" : "bg-emerald-500"}>
                    {status.isRedAlert ? "CRITICAL" : "NORMAL"}
                  </Badge>
                  <Badge variant="outline" className={isOnline ? "border-emerald-500 text-emerald-600" : "border-slate-300 text-slate-500"}>
                    {isOnline ? "ONLINE" : "OFFLINE"}
                  </Badge>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4 items-end">
                <div>
                  <div className="text-4xl font-bold text-emerald-600">
                    {status.temperature != null ? `${status.temperature.toFixed(1)}°` : '--'}
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

              {/* Min / Avg / Max row (from chart history) */}
              <div className="mt-4 grid grid-cols-3 gap-2 text-center text-xs">
                <div className="rounded-xl border border-emerald-100 bg-emerald-50/60 py-3">
                  <p className="text-[10px] text-emerald-500 font-semibold tracking-wide">MIN</p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">
                    {chartStats.min != null ? `${chartStats.min.toFixed(1)}°` : '--'}
                  </p>
                </div>
                <div className="rounded-xl border border-sky-100 bg-sky-50/60 py-3">
                  <p className="text-[10px] text-sky-500 font-semibold tracking-wide">AVG</p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">
                    {chartStats.avg != null ? `${chartStats.avg.toFixed(1)}°` : '--'}
                  </p>
                </div>
                <div className="rounded-xl border border-rose-100 bg-rose-50/60 py-3">
                  <p className="text-[10px] text-rose-500 font-semibold tracking-wide">MAX</p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">
                    {chartStats.max != null ? `${chartStats.max.toFixed(1)}°` : '--'}
                  </p>
                </div>
              </div>

              {/* Gauges for C / F just below stats */}
              <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
                <TemperatureGauge
                  label="Celsius"
                  value={status.temperature ?? null}
                  accentColor={cGaugeColor}
                  min={lowThreshold}
                  max={highThreshold}
                />
                <TemperatureGauge
                  label="Fahrenheit"
                  value={fahrenheit}
                  accentColor={fGaugeColor}
                  min={
                    lowThreshold != null
                      ? (lowThreshold * 9) / 5 + 32
                      : null
                  }
                  max={
                    highThreshold != null
                      ? (highThreshold * 9) / 5 + 32
                      : null
                  }
                />
              </div>

              {/* lowTemp / setTemp / highTemp summary cards from backend */}
              <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-3">
                <div className="rounded-2xl bg-white shadow-sm px-4 py-3 border border-emerald-50">
                  <p className="text-[11px] font-semibold text-slate-500 tracking-[0.08em]">
                    lowTemp
                  </p>
                  <p className="mt-2 text-lg font-semibold text-slate-900">
                    {lowThreshold != null ? `${lowThreshold.toFixed(1)}°C` : '--'}
                  </p>
                </div>
                <div className="rounded-2xl bg-white shadow-sm px-4 py-3 border border-sky-50">
                  <p className="text-[11px] font-semibold text-slate-500 tracking-[0.08em]">
                    setTemp
                  </p>
                  <p className="mt-2 text-lg font-semibold text-slate-900">
                    {setTemp != null ? `${setTemp.toFixed(1)}°C` : '--'}
                  </p>
                </div>
                <div className="rounded-2xl bg-white shadow-sm px-4 py-3 border border-rose-50">
                  <p className="text-[11px] font-semibold text-slate-500 tracking-[0.08em]">
                    highTemp
                  </p>
                  <p className="mt-2 text-lg font-semibold text-slate-900">
                    {highThreshold != null ? `${highThreshold.toFixed(1)}°C` : '--'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Performance chart card */}
          <Card className="flex-[2] shadow-sm">
            <CardHeader className="flex flex-row items-start justify-between gap-3">
              <div>
                <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground">
                  Performance Trends
                </p>
                <CardTitle className="text-base mt-1">
                  Historical monitoring: temperature
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
                      controller.
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
                          value={dateRange.from}
                          onChange={(e) =>
                            setDateRange((prev) => ({ ...prev, from: e.target.value }))
                          }
                          className="h-9 text-sm"
                        />
                        <Input
                          type="date"
                          value={dateRange.to}
                          onChange={(e) =>
                            setDateRange((prev) => ({ ...prev, to: e.target.value }))
                          }
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
              <div className="mt-2">
                {id && (
                  <FreezerChart
                    freezerId={id}
                    fromDate={dateRange.from}
                    toDate={dateRange.to}
                  />
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* LOWER: Monitoring vs settings */}
        <Tabs defaultValue="monitoring" className="w-full mt-4">
          <TabsList>
            <TabsTrigger value="monitoring">Monitoring</TabsTrigger>
            <TabsTrigger value="settings">Settings</TabsTrigger>
          </TabsList>

          {/* Monitoring grid */}
          <TabsContent value="monitoring" className="mt-4 space-y-6">
            {/* Sensor telemetry */}
            <section>
              <h3 className="text-xs font-semibold tracking-[0.16em] text-slate-500 uppercase mb-3">
                Sensor Telemetry
              </h3>
              <div className="grid gap-3 md:grid-cols-3 lg:grid-cols-6">
                <TelemetryCard
                  label="ambientTemperature"
                  value={
                    status.ambientTemperature != null
                      ? `${status.ambientTemperature.toFixed(1)}°C`
                      : '--'
                  }
                />
                <TelemetryCard
                  label="humidity"
                  value={
                    status.humidity != null
                      ? `${status.humidity.toFixed(1)}%`
                      : '--'
                  }
                />
                <TelemetryCard
                  label="compressorTemp"
                  value={
                    status.compressorTemp != null
                      ? `${status.compressorTemp.toFixed(1)}°C`
                      : '--'
                  }
                />
                <TelemetryCard
                  label="freezerCompressor"
                  value={formatStatusValue(status.freezerCompressor, '--')}
                  isAlarm={
                    status.freezerCompressor === false ||
                    status.freezerCompressor === 'OFF'
                  }
                />
                <TelemetryCard
                  label="condenserTemp"
                  value={
                    status.condenserTemp != null
                      ? `${status.condenserTemp.toFixed(1)}°C`
                      : '--'
                  }
                />
                <TelemetryCard
                  label="batteryPercentage"
                  value={
                    status.batteryPercentage != null
                      ? `${status.batteryPercentage.toFixed(1)}%`
                      : '--'
                  }
                />
              </div>
            </section>

            {/* Power grid */}
            <section>
              <h3 className="text-xs font-semibold tracking-[0.16em] text-slate-500 uppercase mb-3">
                Power Grid
              </h3>
              <div className="grid gap-3 md:grid-cols-3">
                <TelemetryCard
                  label="freezerPower"
                  value={formatStatusValue(
                    status.freezerPower,
                    status.freezerOn ? 'ON' : 'OFF'
                  )}
                />
                <TelemetryCard
                  label="acVoltage"
                  value={
                    status.acVoltage != null ? `${status.acVoltage.toFixed(1)} V` : '--'
                  }
                />
                <TelemetryCard
                  label="acCurrent"
                  value={
                    status.acCurrent != null ? `${status.acCurrent.toFixed(1)} A` : '--'
                  }
                />
              </div>
            </section>

          {/* Alarm matrix */}
            <section>
              <h3 className="text-xs font-semibold tracking-[0.16em] text-slate-500 uppercase mb-3">
                Alarm Matrix
              </h3>
              <div className="grid gap-3 md:grid-cols-3 lg:grid-cols-6">
                <TelemetryCard
                  label="freezerDoor"
                  value={formatStatusValue(status.freezerDoor, status.doorOpen ? 'OPEN' : 'CLOSE')}
                  isAlarm={status.doorOpen === true || status.freezerDoor === 'OPEN'}
                />
                <TelemetryCard
                  label="doorAlarm"
                  value={formatStatusValue(status.doorAlarm, 'OFF')}
                  isAlarm={status.doorAlarm === true || status.doorAlarm === 'ON'}
                />
                <TelemetryCard
                  label="powerAlarm"
                  value={formatStatusValue(status.powerAlarm, 'OFF')}
                  isAlarm={status.powerAlarm === true || status.powerAlarm === 'ON'}
                />
                <TelemetryCard
                  label="lowTempAlarm"
                  value={formatStatusValue(status.lowTempAlarm, 'OFF')}
                  isAlarm={status.lowTempAlarm === true || status.lowTempAlarm === 'ON'}
                />
                <TelemetryCard
                  label="batteryPercentAlarm"
                  value={formatStatusValue(status.batteryPercentAlarm, 'OFF')}
                  isAlarm={status.batteryPercentAlarm === true || status.batteryPercentAlarm === 'ON'}
                />
                <TelemetryCard
                  label="highTempAlarm"
                  value={formatStatusValue(status.highTempAlarm, 'OFF')}
                  isAlarm={status.highTempAlarm === true || status.highTempAlarm === 'ON'}
                />
              </div>
            </section>
          </TabsContent>

          {/* Settings as before */}
          <TabsContent value="settings" className="mt-4">
            <Card>
              <CardHeader><CardTitle>Configuration</CardTitle></CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label>Min Threshold</Label>
                    <Input type="number" value={minThreshold} onChange={e => setMinThreshold(Number(e.target.value))} />
                  </div>
                  <div>
                    <Label>Max Threshold</Label>
                    <Input type="number" value={maxThreshold} onChange={e => setMaxThreshold(Number(e.target.value))} />
                  </div>
                </div>
                <Button onClick={handleSaveSettings} disabled={saving}>
                  {saving ? <Loader2 className="animate-spin w-4 h-4 mr-2"/> : <Save className="w-4 h-4 mr-2"/>} Save Settings
                </Button>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

interface TemperatureGaugeProps {
  label: string;
  value: number | null;
  accentColor: string;
  /** Optional dynamic range. When provided, gauge fill is based on [min, max] instead of a fixed range. */
  min?: number | null;
  max?: number | null;
}

const TemperatureGauge = ({
  label,
  value,
  accentColor,
  min,
  max,
}: TemperatureGaugeProps) => {
  const defaultMin = -100;
  const defaultMax = 50;

  const rangeMin =
    typeof min === 'number' && typeof max === 'number' && min < max
      ? min
      : defaultMin;
  const rangeMax =
    typeof min === 'number' && typeof max === 'number' && min < max
      ? max
      : defaultMax;

  const clamped =
    typeof value === 'number'
      ? Math.min(rangeMax, Math.max(rangeMin, value))
      : null;

  const percent =
    clamped != null ? ((clamped - rangeMin) / (rangeMax - rangeMin)) * 100 : 0;

  const gradient =
    clamped != null
      ? `conic-gradient(${accentColor} ${percent}%, #e5e7eb 0)`
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

/** Format API value for display: booleans as ON/OFF, null/undefined as fallback. */
function formatStatusValue(
  value: string | number | boolean | null | undefined,
  fallback: string
): string {
  if (value === true) return 'ON';
  if (value === false) return 'OFF';
  if (value != null && value !== '') return String(value);
  return fallback;
}

interface TelemetryCardProps {
  label: string;
  value: string;
  /** When true, render the card in an alarm (red) style. */
  isAlarm?: boolean;
}

const TelemetryCard = ({ label, value, isAlarm }: TelemetryCardProps) => (
  <div
    className={`rounded-2xl px-4 py-3 shadow-sm border ${
      isAlarm
        ? 'border-red-200 bg-red-50/80'
        : 'border-slate-100 bg-white/70'
    }`}
  >
    <p
      className={`text-[11px] font-semibold tracking-[0.04em] ${
        isAlarm ? 'text-red-600' : 'text-slate-500'
      }`}
    >
      {label}
    </p>
    <p
      className={`mt-1 text-sm font-semibold ${
        isAlarm ? 'text-red-700' : 'text-slate-900'
      }`}
    >
      {value}
    </p>
  </div>
);

export default FreezerDetail;