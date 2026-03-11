import axios from 'axios';
import toast from 'react-hot-toast';

const API_GATEWAY_URL = 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_GATEWAY_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
apiClient.interceptors.request.use(
  (config) => {

    const token = localStorage.getItem('accessToken');

    // ✅ Public endpoints (NO TOKEN)
    const PUBLIC_ROUTES = [
      '/auth/login',
      '/auth/signup',
      '/auth/verify-otp',
      '/auth/resend-otp'
    ];

    const isPublicRoute = PUBLIC_ROUTES.some(route =>
      config.url?.includes(route)
    );

    // ✅ Attach token ONLY for protected APIs
    if (token && !isPublicRoute) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add X-User-Id header for freezer service endpoints
    const userId = localStorage.getItem('userId');
    if (userId && config.url?.includes('/freezers')) {
      config.headers['X-User-Id'] = userId;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;

    // 🔥 If unauthorized → logout + redirect
    if (status === 401) {
      localStorage.clear();
      window.location.href = "/login";
    }

    const errorMessage =
      error.response?.data?.message ||
      error.message ||
      'An error occurred';

    toast.error(errorMessage);
    return Promise.reject(error);
  }
);

export default apiClient;
