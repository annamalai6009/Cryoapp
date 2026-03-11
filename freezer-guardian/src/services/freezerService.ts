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
    const response = await apiClient.get<FreezerDetailResponse[]>(
      `/freezers/api/internal/${userId}`
    );

    return response.data.map((item, index) => {
      const topicId = item.freezerId || null;
      const hasTopic = !!topicId;

      // Use topicId for routing when available; otherwise fall back to PO or a stable row key
      const id =
        topicId ||
        item.poNumber ||
        `row-${index}`;

      return {
        id,
        topicId,
        hasTopic,
        name: item.name,
        poNumber: item.poNumber,
        currentTemperature: item.currentTemp,
        status: item.status,
        isRedAlert: item.isRedAlert,
        lastUpdated: item.lastUpdate,
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