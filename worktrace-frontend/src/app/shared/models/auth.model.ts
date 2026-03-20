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

export interface PasswordChangeRequest {
  actual: string;
  nueva: string;
  repetir: string;
}

export interface GenericMessageResponse {
  message: string;
}
