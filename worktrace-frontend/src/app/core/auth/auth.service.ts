import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { API_CONFIG } from '../api/api.config';
import {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  PasswordChangeRequest,
  GenericMessageResponse,
  ResetPasswordRequest,
} from '../../shared/models/auth.model';
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private tokenStorage = inject(TokenStorageService);

  private readonly BASE_URL = API_CONFIG.baseUrl;

  private loggedIn = new BehaviorSubject<boolean>(!!this.tokenStorage.getToken());
  isLoggedIn$ = this.loggedIn.asObservable();

  constructor() {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}auth/login`, credentials).pipe(
      tap((response: AuthResponse) => {
        if (response.token) {
          this.tokenStorage.saveToken(response.token);
          this.loggedIn.next(true);
        }
      }),
    );
  }

  logout(): void {
    this.tokenStorage.clear();
    this.loggedIn.next(false);
  }

  solicitarRecuperacion(email: string): Observable<GenericMessageResponse> {
    const request: ForgotPasswordRequest = { email };
    return this.http.post<GenericMessageResponse>(`${this.BASE_URL}auth/forgot-password`, request);
  }

  ejecutarResetPassword(request: ResetPasswordRequest): Observable<GenericMessageResponse> {
    return this.http.post<GenericMessageResponse>(`${this.BASE_URL}auth/reset-password`, request);
  }

  changeFirstPassword(passwordData: PasswordChangeRequest): Observable<GenericMessageResponse> {
    return this.http.patch<GenericMessageResponse>(`${this.BASE_URL}auth/password`, passwordData);
  }
}
