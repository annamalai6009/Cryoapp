import apiClient from './apiClient';

export const exportService = {
  async exportToPdf(freezerId: string, from: string, to: string): Promise<Blob> {
    const response = await apiClient.get(`/export/freezers/${freezerId}/pdf`, {
      params: { from, to },
      responseType: 'blob',
    });
    return response.data;
  },

  async exportToCsv(freezerId: string, from: string, to: string): Promise<Blob> {
    const response = await apiClient.get(`/export/freezers/${freezerId}/csv`, {
      params: { from, to },
      responseType: 'blob',
    });
    return response.data;
  },

  // Helper to trigger download
  downloadBlob(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.parentNode?.removeChild(link);
    window.URL.revokeObjectURL(url);
  },
};
