import apiClient from './apiClient';

export interface AlertEvaluateRequest {
  freezerId: string;
  temperature: number;
  minThreshold: number;
  maxThreshold: number;
}

export interface AlertEvaluateResponse {
  isAlert: boolean;
  alertType: 'HIGH_TEMP' | 'LOW_TEMP' | 'NONE';
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const alertService = {
  async evaluateAlert(data: AlertEvaluateRequest): Promise<ApiResponse<AlertEvaluateResponse>> {
    const response = await apiClient.post('/alerts/evaluate', data);
    return response.data;
  },
};
