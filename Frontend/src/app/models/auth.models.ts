export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  id: number;
  email: string;
  fullName: string;
  role: 'USER' | 'ADMIN' | string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ForgotPasswordResponse {
  message: string;
  resetToken: string | null;
  resetLink: string | null;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}