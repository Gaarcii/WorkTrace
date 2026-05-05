export interface AuthResponse {
  token: string;
  firstLogin: boolean;
  role: string;
  error: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  repeatPassword: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  repeatPassword: string;
}

export interface GenericMessageResponse {
  message: string;
}
