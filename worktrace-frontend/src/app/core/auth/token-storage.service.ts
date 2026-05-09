import { Injectable } from '@angular/core';

/**
 * @class TokenStorageService
 * @description
 * Servicio responsable de gestionar el almacenamiento y la recuperación de datos de autenticación
 * (como el token JWT y el rol del usuario) en el `localStorage` del navegador.
 * Actúa como una capa de abstracción sobre el `localStorage` para centralizar la lógica
 * de persistencia de la sesión del usuario.
 */
@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private readonly TOKEN_KEY = 'worktrace_jwt';
  private readonly ROLE_KEY = 'worktrace_role';

  constructor() { }

  /**
   * Guarda el token de autenticación JWT en el `localStorage`.
   *
   * @description
   * Elimina cualquier token existente antes de guardar el nuevo para evitar duplicados
   * o conflictos.
   *
   * @param token - El token JWT en formato de cadena de texto que se va a almacenar.
   */
  public saveToken(token: string): void {
    window.localStorage.removeItem(this.TOKEN_KEY);
    window.localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Recupera el token de autenticación JWT del `localStorage`.
   *
   * @returns El token JWT como una cadena de texto si existe, o `null` si no se encuentra.
   */
  public getToken(): string | null {
    return window.localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Guarda el rol del usuario en el `localStorage`.
   *
   * @description
   * Almacena el rol del usuario para poder realizar comprobaciones de autorización
   * sin necesidad de decodificar el token en cada ocasión.
   *
   * @param role - El rol del usuario (ej. 'ADMIN', 'USER') en formato de cadena de texto.
   */
  public saveRole(role: string): void {
    window.localStorage.removeItem(this.ROLE_KEY);
    window.localStorage.setItem(this.ROLE_KEY, role);
  }

  /**
   * Recupera el rol del usuario del `localStorage`.
   *
   * @returns El rol del usuario como una cadena de texto si existe, o `null` si no se encuentra.
   */
  public getRole(): string | null {
    return window.localStorage.getItem(this.ROLE_KEY);
  }

  /**
   * Elimina todos los datos de autenticación del `localStorage`.
   *
   * @description
   * Este método se utiliza típicamente durante el proceso de cierre de sesión para
   * limpiar completamente la sesión del usuario del navegador.
   */
  public clear(): void {
    window.localStorage.clear();
  }
}
