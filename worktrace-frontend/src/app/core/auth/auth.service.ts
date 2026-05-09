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

/**
 * @class AuthService
 * @description
 * Servicio encargado de gestionar la autenticación de usuarios.
 * Proporciona métodos para el inicio de sesión, cierre de sesión, y recuperación de contraseña,
 * interactuando con el backend y gestionando el estado de autenticación del usuario.
 */
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

  /**
   * Envía las credenciales del usuario al backend para iniciar sesión.
   *
   * @description
   * Realiza una petición HTTP POST a la ruta de login. Si la autenticación es exitosa,
   * guarda el token JWT recibido en el almacenamiento local y actualiza el estado
   * de autenticación a `true`.
   *
   * @param credentials - Un objeto `LoginRequest` con el email y la contraseña del usuario.
   * @returns Un `Observable` que emite una respuesta `AuthResponse` con el token de acceso.
   */
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

  /**
   * Cierra la sesión del usuario actual.
   *
   * @description
   * Elimina el token de autenticación del almacenamiento local y actualiza el estado
   * de autenticación a `false`, notificando a los suscriptores del cambio.
   */
  logout(): void {
    this.tokenStorage.clear();
    this.loggedIn.next(false);
  }

  /**
   * Inicia el proceso de recuperación de contraseña para un usuario.
   *
   * @description
   * Envía una petición HTTP POST al backend con el correo electrónico del usuario
   * para que el sistema le envíe las instrucciones de recuperación.
   *
   * @param email - El correo electrónico del usuario que solicita la recuperación.
   * @returns Un `Observable` que emite una respuesta `GenericMessageResponse` con un mensaje de confirmación.
   */
  solicitarRecuperacion(email: string): Observable<GenericMessageResponse> {
    const request: ForgotPasswordRequest = { email };
    return this.http.post<GenericMessageResponse>(`${this.BASE_URL}auth/forgot-password`, request);
  }

  /**
   * Completa el proceso de reseteo de contraseña.
   *
   * @description
   * Envía el token de reseteo y la nueva contraseña al backend para actualizar
   * las credenciales del usuario.
   *
   * @param request - Un objeto `ResetPasswordRequest` que contiene el token y la nueva contraseña.
   * @returns Un `Observable` que emite una respuesta `GenericMessageResponse` con un mensaje de confirmación.
   */
  ejecutarResetPassword(request: ResetPasswordRequest): Observable<GenericMessageResponse> {
    return this.http.post<GenericMessageResponse>(`${this.BASE_URL}auth/reset-password`, request);
  }

  /**
   * Permite al usuario cambiar su contraseña por primera vez o cuando es requerido.
   *
   * @description
   * Realiza una petición HTTP PATCH para actualizar la contraseña del usuario autenticado.
   * Se utiliza típicamente después del primer inicio de sesión con una contraseña temporal.
   *
   * @param passwordData - Un objeto `PasswordChangeRequest` con la contraseña actual y la nueva.
   * @returns Un `Observable` que emite una respuesta `GenericMessageResponse` con un mensaje de confirmación.
   */
  changeFirstPassword(passwordData: PasswordChangeRequest): Observable<GenericMessageResponse> {
    return this.http.patch<GenericMessageResponse>(`${this.BASE_URL}auth/password`, passwordData);
  }
}
