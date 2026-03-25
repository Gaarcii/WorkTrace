import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, Observable } from 'rxjs';
import { ProfileRequest, ProfileResponse } from '../models/profile.model';
import { API_CONFIG } from '../../core/api/api.config';
import { TokenStorageService } from '../../core/auth/token-storage.service';

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  private readonly tokenStorageService = inject(TokenStorageService);

  readonly currentUser = signal<ProfileResponse | null>(null);

  /**
   * Obtiene el perfil del usuario actual
   * Endpoint: GET /api/user/profile
   */
  fetchMyProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.BASE_URL}user/profile`).pipe(
      tap((profile: ProfileResponse) => {
        this.currentUser.set(profile);
      }),
    );
  }

  /**
   * Actualiza el perfil del usuario actual (soporta subida de avatar)
   * Endpoint: PATCH /api/user (Multipart Form Data)
   */
  updateMyProfile(request: ProfileRequest): Observable<ProfileResponse> {
    // Al requerir MULTIPART_FORM_DATA_VALUE en Spring Boot, construimos un FormData
    const formData = new FormData();

    formData.append('email', request.email);
    formData.append('telefono', request.telefono);
    formData.append('contrasenaActual', request.contrasenaActual);
    formData.append('eliminarAvatar', request.eliminarAvatar);

    // Adjuntamos el archivo binario solo si el usuario seleccionó uno
    if (request.avatar) {
      formData.append('avatar', request.avatar);
    }

    // Nota: Según tu captura, el @PatchMapping no tiene sub-ruta,
    // por lo que apunta a la raíz del @RequestMapping del controlador ("user")
    return this.http.patch<ProfileResponse>(`${this.BASE_URL}user`, formData).pipe(
      tap((profile: ProfileResponse) => {
        if (profile.tokenActualizado != null) {
          this.tokenStorageService.saveToken(profile.tokenActualizado);
        }
        this.currentUser.set(profile);
      }),
    );
  }
}
