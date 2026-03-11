import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { DashboardLayout } from '@/components/DashboardLayout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  freezerService,
  FreezerStatusResponse,
  FreezerConfigResponse,
} from '@/services/freezerService';
import { format, subDays, differenceInMinutes } from 'date-fns';
import { Loader2 } from 'lucide-react';

type DataLoggerChannel = {
  channelNumber: number | string;
  temperature: number | null;
  status: string | boolean | null;
  highAlarm?: boolean | string | null;
  lowAlarm?: boolean | string | null;
  timestamp: string;
};

type DataLoggerStatus = FreezerStatusResponse & {
  ambientTemperature?: number | null;
  batteryPercentage?: number | null;
  power?: string | null;
  channels?: DataLoggerChannel[];
};

const DataLoggerDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [status, setStatus] = useState<DataLoggerStatus | null>(null);
  const [config, setConfig] = useState<FreezerConfigResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      const [statusData, configData] = await Promise.all([
        freezerService.getFreezerStatus(id),
        freezerService.getFreezerConfig(id),
      ]);
      setStatus(statusData as unknown as DataLoggerStatus);
      if (configData) {
        setConfig(configData);
      }
    } catch (error) {
      console.error('Data logger fetch failed', error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex h-screen items-center justify-center">
          <Loader2 className="animate-spin" />
        </div>
      </DashboardLayout>
    );
  }

  if (!status) {
    return (
      <DashboardLayout>
        <div className="p-10 text-center">Data logger not found</div>
      </DashboardLayout>
    );
  }

  // Online/offline based on last device timestamp (10-minute window)
  const lastUpdate =
    status.timestamp ? new Date(status.timestamp) : null;
  const minutesSinceUpdate =
    lastUpdate ? differenceInMinutes(new Date(), lastUpdate) : Number.POSITIVE_INFINITY;
  const isOnline = Number.isFinite(minutesSinceUpdate) && minutesSinceUpdate <= 10;

  const rawChannels = status.channels || [];
  // Deduplicate channels by channelNumber to avoid visual duplicates
  const channelMap = new Map<string | number, DataLoggerChannel>();
  for (const ch of rawChannels) {
    if (!channelMap.has(ch.channelNumber)) {
      channelMap.set(ch.channelNumber, ch);
    }
  }
  const channels = Array.from(channelMap.values());

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-slate-900">Data Logger Dashboard</h1>

        {/* Top: summary only */}
        <div className="mt-4 flex flex-col xl:flex-row gap-6">
          {/* Summary card */}
          <Card className="flex-1 shadow-sm">
            <CardHeader className="space-y-2">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground">
                    Data Logger Status
                  </p>
                  <h1 className="text-lg font-semibold text-slate-900 mt-1">
                    {config?.name || id}
                  </h1>
                  <p className="text-[11px] text-slate-400 font-mono mt-1">ID: {id}</p>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <Badge variant="outline" className="text-[11px] px-3 py-1 rounded-full">
                    {status.power || 'UNKNOWN'}
                  </Badge>
                  <Badge variant="outline" className={isOnline ? "border-emerald-500 text-emerald-600" : "border-slate-300 text-slate-500"}>
                    {isOnline ? "ONLINE" : "OFFLINE"}
                  </Badge>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-3 gap-3 text-sm">
                <div className="rounded-xl border border-slate-100 bg-white/70 px-3 py-2">
                  <p className="text-[11px] uppercase tracking-[0.14em] text-slate-500">
                    Ambient Temp
                  </p>
                  <p className="mt-1 font-semibold text-slate-900">
                    {status.ambientTemperature != null
                      ? `${status.ambientTemperature.toFixed(1)}°C`
                      : '--'}
                  </p>
                </div>
                <div className="rounded-xl border border-slate-100 bg-white/70 px-3 py-2">
                  <p className="text-[11px] uppercase tracking-[0.14em] text-slate-500">Battery</p>
                  <p className="mt-1 font-semibold text-slate-900">
                    {status.batteryPercentage != null
                      ? `${status.batteryPercentage.toFixed(1)}%`
                      : '--'}
                  </p>
                </div>
                <div className="rounded-xl border border-slate-100 bg-white/70 px-3 py-2">
                  <p className="text-[11px] uppercase tracking-[0.14em] text-slate-500">
                    Channels
                  </p>
                  <p className="mt-1 font-semibold text-slate-900">{channels.length || '--'}</p>
                </div>
              </div>
            </CardContent>
          </Card>

        </div>

        {/* Lower: channels grid */}
        <Tabs defaultValue="channels" className="w-full mt-4">
          <TabsList>
            <TabsTrigger value="channels">Channels</TabsTrigger>
            <TabsTrigger value="settings">Settings</TabsTrigger>
          </TabsList>

          <TabsContent value="channels" className="mt-4">
            <Card>
              <CardHeader>
                <CardTitle>Channels</CardTitle>
              </CardHeader>
              <CardContent>
                {channels.length === 0 ? (
                  <div className="py-8 text-center text-sm text-muted-foreground">
                    No channel data available yet.
                  </div>
                ) : (
                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                    {channels.map((ch) => {
                      const channelLabel =
                        typeof ch.channelNumber === 'string'
                          ? ch.channelNumber.toUpperCase().startsWith('CH')
                            ? ch.channelNumber.toUpperCase()
                            : `CH${ch.channelNumber}`
                          : `CH${ch.channelNumber}`;
                      const isOn =
                        typeof ch.status === 'boolean'
                          ? ch.status
                          : typeof ch.status === 'string'
                          ? ch.status.toUpperCase() === 'ON'
                          : false;
                      return (
                        <button
                          type="button"
                          key={ch.channelNumber}
                          onClick={() => navigate(`/datalogger/${id}/channel/${ch.channelNumber}`)}
                          className="text-left rounded-2xl border px-4 py-3 shadow-sm transition-all border-slate-100 bg-white/90 hover:border-slate-300 hover:shadow-md"
                        >
                          <div className="flex items-center justify-between">
                            <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-[0.14em]">
                              Channel {channelLabel}
                            </p>
                            <span
                              className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold ${
                                isOn
                                  ? 'bg-emerald-50 text-emerald-700 border border-emerald-100'
                                  : 'bg-slate-50 text-slate-500 border border-slate-100'
                              }`}
                            >
                              {isOn ? 'ON' : 'OFF'}
                            </span>
                          </div>
                          <p className="mt-2 text-xl font-bold text-slate-900">
                            {ch.temperature != null ? `${ch.temperature.toFixed(1)}°C` : '--'}
                          </p>
                          <p className="mt-1 text-[11px] text-slate-400">
                            Status:{' '}
                            <span className="font-mono">
                              {typeof ch.status === 'boolean'
                                ? ch.status
                                  ? 'ON'
                                  : 'OFF'
                                : ch.status || 'UNKNOWN'}
                            </span>
                          </p>
                        </button>
                      );
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="settings" className="mt-4">
            <Card>
              <CardHeader>
                <CardTitle>Configuration</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Data logger configuration will be available in a future version.
                </p>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default DataLoggerDetails;

