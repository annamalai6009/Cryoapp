import apiClient from './apiClient';

export interface ChartDataPoint {
  timestamp: string;
  temperature: number;
  freezerOn: boolean;
  doorOpen: boolean;
}

export interface FreezerDetailResponse {
  freezerId: string;
  name: string;
  poNumber: string;
  status: string;
  currentTemp: number;
  isFreezerOn: boolean;
  isDoorOpen: boolean;
  lastUpdate: string;
  isRedAlert: boolean;
  oneMinuteAvgTemp: number;
}

type DashboardControllerRow = {
  topic?: string | null;
  po?: string | null;
  timestamp?: string | null;
  temperature?: number | string | null;
  freezerPower?: string | boolean | null;
};

type DashboardDataLoggerRow = {
  common?: {
    topic?: string | null;
    topicId?: string | null;
    topic_id?: string | null;
    deviceId?: string | null;
    deviceID?: string | null;
    dataloggerId?: string | null;
    dataLoggerId?: string | null;
    po?: string | null;
    timestamp?: string | null;
    power?: string | boolean | null;
  } | null;
  channels?: Array<{
    channelNumber?: string | number | null;
    temperature?: number | string | null;
    status?: string | boolean | null;
  }> | null;
};

export interface FreezerStatusResponse {
  temperature: number;
  freezerOn: boolean;
  doorOpen: boolean;
  timestamp: string;
  isRedAlert: boolean;

  // Optional extended telemetry fields from backend JSON
  ambientTemperature?: number | null;
  humidity?: number | null;

  freezerDoor?: string | null;
  doorAlarm?: boolean | string | null;
  freezerPower?: boolean | string | null;
  powerAlarm?: boolean | string | null;

  compressorTemp?: number | null;
  freezerCompressor?: boolean | string | null;
  condenserTemp?: number | null;

  setTemp?: number | null;
  highTemp?: number | null;
  lowTemp?: number | null;
  highTempAlarm?: boolean | string | null;
  lowTempAlarm?: boolean | string | null;

  batteryPercentage?: number | null;
  batteryPercentAlarm?: boolean | string | null;

  acVoltage?: number | null;
  acCurrent?: number | null;
}

export interface FreezerSummary {
  // Normal freezer metrics
  totalFreezers: number;
  activeFreezersCount: number;
  freezersOnCount: number;
  freezersOffCount: number;
  redAlertFreezersCount: number;

  // Data logger metrics (also returned by backend)
  totalDataLoggers?: number;
  totalChannels?: number;
  channelsSending?: number;
  channelsNotSending?: number;
  channelsInAlert?: number;

  // Combined metrics
  activeDevicesCount?: number;
}

export interface FreezerResponse {
  id: string;              // stable row key & routing ID
  topicId?: string | null; // actual freezerId/topic from backend
  hasTopic: boolean;       // true when topicId is set
  name: string;
  poNumber: string;
  currentTemperature: number;
  status: string;
  isRedAlert: boolean;
  lastUpdated: string;
  oneMinuteAvgTemp?: number;
  freezerOn?: boolean;
  doorOpen?: boolean;
}

export interface FreezerConfigResponse {
  name?: string;
  minThreshold: number;
  maxThreshold: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

/* ✅ NEW REQUEST TYPES (NO MORE ANY) */

export interface RegisterFreezerRequest {
  name: string;
  poNumber: string;
}

export interface RegisterFreezerResponse {
  freezerId: string;
  message: string;
}

export type DeviceType = 'NORMAL_FREEZER' | 'DATA_LOGGER';

export interface InventoryItem {
  poNumber: string;
  s3Url: string;
  deviceType: DeviceType;
}

export interface UpdateSettingsRequest {
  minThreshold?: number;
  maxThreshold?: number;
}

/* ================================================= */

export const freezerService = {

  async getDashboardData(userId: string): Promise<FreezerResponse[]> {
    // Backend returns mixed device shapes (controller vs data logger) depending on device type.
    // Use unknown[] and normalize into FreezerResponse for the UI.
    const response = await apiClient.get<unknown[]>(
      `/freezers/api/internal/${userId}`
    );

    const toNonEmptyString = (v: unknown): string | null => {
      if (typeof v !== 'string') return null;
      const s = v.trim();
      return s.length > 0 ? s : null;
    };

    const toNumberOrNull = (v: unknown): number | null => {
      if (typeof v === 'number' && Number.isFinite(v)) return v;
      if (typeof v === 'string') {
        const n = Number.parseFloat(v);
        return Number.isFinite(n) ? n : null;
      }
      return null;
    };

    return (response.data || []).map((raw, index) => {
      const item = raw as Partial<FreezerDetailResponse> &
        DashboardControllerRow &
        DashboardDataLoggerRow & {
          freezerId?: string | null;
          topicId?: string | null;
          topic_id?: string | null;
          poNumber?: string | null;
          po?: string | null;
        };

      // Topic can be on controller rows (`topic`) OR inside datalogger rows (`common.topic`) OR legacy (`freezerId` etc).
      const topicId =
        toNonEmptyString(item.freezerId) ??
        toNonEmptyString(item.topicId) ??
        toNonEmptyString(item.topic_id) ??
        toNonEmptyString(item.topic) ??
        toNonEmptyString(item.common?.topic) ??
        toNonEmptyString(item.common?.topicId) ??
        toNonEmptyString(item.common?.topic_id) ??
        toNonEmptyString(item.common?.deviceId) ??
        toNonEmptyString(item.common?.deviceID) ??
        toNonEmptyString(item.common?.dataLoggerId) ??
        toNonEmptyString(item.common?.dataloggerId) ??
        null;

      const hasTopic = topicId !== null;

      // PO can be `poNumber` (old) or `po` (controller) or `common.po` (datalogger)
      const poNumber =
        toNonEmptyString(item.poNumber) ??
        toNonEmptyString(item.po) ??
        toNonEmptyString(item.common?.po) ??
        '';

      // Temperature can be `currentTemp` (old) or `temperature` (controller). For datalogger list, pick CH1 temp if present.
      const temperature =
        toNumberOrNull(item.currentTemp) ??
        toNumberOrNull(item.temperature) ??
        toNumberOrNull(item.channels?.[0]?.temperature) ??
        0;

      const lastUpdated =
        toNonEmptyString(item.lastUpdate) ??
        toNonEmptyString(item.timestamp) ??
        toNonEmptyString(item.common?.timestamp) ??
        '';

      // Status: old `status` or controller `freezerPower` or datalogger `common.power`
      const status =
        toNonEmptyString(item.status) ??
        toNonEmptyString(item.freezerPower) ??
        toNonEmptyString(item.common?.power) ??
        'UNKNOWN';

      // Name: use backend `name` when available; otherwise label by topic prefix.
      const name =
        toNonEmptyString(item.name) ??
        (topicId?.toUpperCase().startsWith('DL') ? 'Data Logger' : 'Controller');

      // Use topicId for routing when available; otherwise fall back to PO or a stable row key
      const id = topicId || poNumber || `row-${index}`;

      return {
        id,
        topicId,
        hasTopic,
        name,
        poNumber,
        currentTemperature: temperature,
        status,
        isRedAlert: Boolean(item.isRedAlert ?? false),
        lastUpdated,
        oneMinuteAvgTemp: item.oneMinuteAvgTemp,
        freezerOn: item.isFreezerOn,
        doorOpen: item.isDoorOpen,
      };
    });
  },

  async getFreezerSummary(): Promise<ApiResponse<FreezerSummary>> {
    const response = await apiClient.get<ApiResponse<FreezerSummary>>(
      '/freezers/summary'
    );
    return response.data;
  },

  async registerFreezer(
    data: RegisterFreezerRequest
  ): Promise<ApiResponse<RegisterFreezerResponse>> {

    const response = await apiClient.post<
      ApiResponse<RegisterFreezerResponse>
    >('/freezers/register', data);

    return response.data;
  },

  async getFreezerStatus(
    freezerId: string
  ): Promise<FreezerStatusResponse> {

    const response = await apiClient.get<
      ApiResponse<FreezerStatusResponse>
    >(`/freezers/${freezerId}/status`);

    return response.data.data;
  },

  async getFreezerChart(
    freezerId: string,
    from: string,
    to: string
  ): Promise<ChartDataPoint[]> {

    const response = await apiClient.get<ApiResponse<ChartDataPoint[]>>(
      `/freezers/${freezerId}/chart`,
      {
        params: { from, to, channel: '1' }
      }
    );

    return response.data.data;
  },

  async getFreezerChannelChart(
    freezerId: string,
    channel: string,
    from: string,
    to: string
  ): Promise<ChartDataPoint[]> {
    const response = await apiClient.get<ApiResponse<ChartDataPoint[]>>(
      `/freezers/${freezerId}/chart`,
      {
        params: { from, to, channel }
      }
    );

    return response.data.data;
  },

  async addInventory(items: InventoryItem[]): Promise<ApiResponse<string>> {
    const response = await apiClient.post<ApiResponse<string>>(
      '/freezers/admin/inventory',
      items
    );
    return response.data;
  },

  async getFreezerConfig(
    freezerId: string
  ): Promise<FreezerConfigResponse | null> {

    const response = await apiClient.get<
      ApiResponse<FreezerConfigResponse>
    >(`/freezers/${freezerId}/config`);

    return response.data.data;
  },

  async updateSettings(
    freezerId: string,
    data: UpdateSettingsRequest
  ): Promise<void> {

    await apiClient.put(
      `/freezers/${freezerId}/settings`,
      data
    );
  }
};