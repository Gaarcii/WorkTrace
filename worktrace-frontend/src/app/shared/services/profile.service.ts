import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, Observable } from 'rxjs';
import { ProfileRequest, ProfileResponse } from '../models/profile.model';
import { API_CONFIG } from '../../core/api/api.config';
import { TokenStorageService } from '../../core/auth/token-storage.service';

/**
 * @class ProfileService
 * @description
 * Servicio encargado de gestionar la información del perfil del usuario autenticado.
 * Proporciona métodos para obtener y actualizar los datos del perfil, interactuando
 * con los endpoints correspondientes de la API.
 */
@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  private readonly tokenStorageService = inject(TokenStorageService);

  readonly currentUser = signal<ProfileResponse | null>(null);

  /**
   * Recupera el perfil del usuario actualmente autenticado desde el backend.
   *
   * @description
   * Realiza una petición HTTP GET para obtener los datos del perfil y, si tiene éxito,
   * actualiza el estado local (`currentUser` signal) con la información recibida.
   *
   * @returns Un `Observable` que emite un objeto `ProfileResponse` con los datos del perfil.
   */
  fetchMyProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.BASE_URL}user/profile`).pipe(
      tap((profile: ProfileResponse) => {
        this.currentUser.set(profile);
      }),
    );
  }

  /**
   * Actualiza los datos del perfil del usuario autenticado.
   *
   * @description
   * Envía una petición HTTP PATCH con los datos del perfil a actualizar.
   * Construye un objeto `FormData` para poder enviar tanto datos de texto como
   * un archivo de imagen (avatar) en una sola petición `multipart/form-data`.
   * Si la actualización es exitosa y el backend devuelve un nuevo token (ej. por cambio de email),
   * este se guarda en el almacenamiento.
   *
   * @param request - Un objeto `ProfileRequest` que contiene los campos a modificar.
   * @returns Un `Observable` que emite un objeto `ProfileResponse` con el perfil ya actualizado.
   */
  updateMyProfile(request: ProfileRequest): Observable<ProfileResponse> {
    const formData = new FormData();

    if (request.email) {
      formData.append('email', request.email);
    }
    if (request.phone) {
      formData.append('telefono', request.phone);
    }
    if (request.actualPassword) {
      formData.append('actualPassword', request.actualPassword);
    }
    if (request.deleteAvatar) {
      formData.append('deleteAvatar', request.deleteAvatar);
    }
    if (request.avatar) {
      formData.append('avatar', request.avatar);
    }

    // Log para depuración
    console.log('Datos a enviar en FormData:');
    formData.forEach((value, key) => {
      console.log(`${key}:`, value);
    });

    return this.http.patch<ProfileResponse>(`${this.BASE_URL}user`, formData).pipe(
      tap((profile: ProfileResponse) => {
        if (profile.updatedToken != null) {
          this.tokenStorageService.saveToken(profile.updatedToken);
        }
        this.currentUser.set(profile);
      }),
    );
  }
}
