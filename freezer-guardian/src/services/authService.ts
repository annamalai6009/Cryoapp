import apiClient from './apiClient';

// ==========================================
// ✅ Interfaces (Definitions)
// ==========================================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  mobileNumber: string;
  password: string;
}

export interface OtpVerificationRequest {
  email: string;
  otp: string; 
}

// ✅ NEW: Added this interface for Profile Updates
export interface UpdateProfileRequest {
  mobileNumber?: string;
  alternativeMobileNumber?: string;
  notifyWhatsapp?: boolean;
  notifySms?: boolean;
  notifyEmail?: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  ownerUserId: string;
  email: string;
  roles: string | string[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

// ==========================================
// ✅ Auth Service Implementation
// ==========================================

export const authService = {
  
  async login(data: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await apiClient.post('/auth/login', data);
    return response.data;
  },

  async signup(data: SignupRequest): Promise<ApiResponse<void>> {
    const response = await apiClient.post('/auth/signup', data);
    return response.data;
  },

  async verifyOtp(data: OtpVerificationRequest): Promise<ApiResponse<void>> {
    const payload = {
      email: data.email,
      otpCode: data.otp 
    };
    const response = await apiClient.post('/auth/verify-otp', payload);
    return response.data;
  },

  async resendOtp(data: { email: string }): Promise<ApiResponse<void>> {
    const response = await apiClient.post('/auth/resend-otp', data);
    return response.data;
  },

  // ✅ Admin: Create Staff
  async createStaff(data: SignupRequest): Promise<ApiResponse<string>> {
    const response = await apiClient.post('/auth/admin/create-staff', data);
    return response.data;
  },

  // ✅ Admin: Deactivate Staff
  async deactivateStaff(email: string): Promise<ApiResponse<string>> {
    const response = await apiClient.put(`/auth/admin/deactivate-staff/${email}`);
    return response.data;
  },

  // ✅ Get User Profile (Includes Notification Settings)
  async getProfile(): Promise<ApiResponse<any>> {
    const response = await apiClient.get('/auth/profile');
    return response.data;
  },

  // ✅ Update Profile (Saves Mobile & Notification Settings)
  async updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<any>> {
    const response = await apiClient.put('/auth/profile', data);
    return response.data;
  },

  // ==========================================
  // ✅ Local Storage Helpers
  // ==========================================

  setAuthData(authResponse: AuthResponse) {
    localStorage.setItem('accessToken', authResponse.accessToken);
    localStorage.setItem('refreshToken', authResponse.refreshToken);
    localStorage.setItem('userId', authResponse.ownerUserId);
    
    if (authResponse.email) {
      localStorage.setItem('userEmail', authResponse.email);
    }
    
    const roles = Array.isArray(authResponse.roles) 
      ? authResponse.roles 
      : [authResponse.roles as string];
    localStorage.setItem('userRoles', JSON.stringify(roles));
  },

  clearAuthData() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRoles');
  },
};