import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { AdminIncidenceRequestDto, AdminIncidenceResponseDto } from '../../models/incidence.model';
import { AdminIncidenciaView } from '../../../features/admin/incidencias/admin-incidencias.types';

interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}

/**
 * @class AdminIncidenciasService
 * @description
 * Servicio para la gestión de incidencias desde la perspectiva del administrador.
 * Se encarga de obtener las listas de incidencias pendientes e históricas,
 * así como de procesar las acciones de aprobación o rechazo sobre ellas.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminIncidenciasService {
  private readonly http = inject(HttpClient);

  private readonly BASE_URL = API_CONFIG.baseUrl + 'incidences';

  readonly incidenciasPendientesSignal = signal<AdminIncidenciaView[]>([]);
  readonly incidenciasHistorialSignal = signal<AdminIncidenciaView[]>([]);

  /**
   * Obtiene la lista de incidencias que están pendientes de revisión.
   *
   * @description
   * Realiza una petición HTTP GET para obtener una página de incidencias con estado 'PENDING'.
   * Transforma los datos recibidos (DTO) a un modelo de vista y actualiza el
   * `incidenciasPendientesSignal` con el resultado.
   *
   * @param page - El número de página a solicitar (basado en 0).
   * @param size - El número de incidencias por página.
   * @returns Un `Observable` que emite un array del modelo de vista `AdminIncidenciaView`.
   */
  obtenerPendientes(page = 0, size = 100): Observable<AdminIncidenciaView[]> {
    const params = new HttpParams().set('status', 'PENDING').set('page', page).set('size', size);

    return this.http
      .get<SpringPageResponse<AdminIncidenceResponseDto>>(`${this.BASE_URL}/admin`, { params })
      .pipe(
        map((response) => (response.content ?? []).map((item) => this.mapDtoToView(item))),
        tap((incidencias) => this.incidenciasPendientesSignal.set(incidencias)),
      );
  }

  /**
   * Obtiene el historial de incidencias que ya han sido gestionadas (resueltas o rechazadas).
   *
   * @description
   * Realiza una petición HTTP GET para obtener el historial paginado de incidencias.
   * Transforma los datos y actualiza el `incidenciasHistorialSignal` con el resultado.
   *
   * @param page - El número de página a solicitar (basado en 0).
   * @param size - El número de incidencias por página.
   * @returns Un `Observable` que emite un array del modelo de vista `AdminIncidenciaView`.
   */
  obtenerHistorial(page = 0, size = 100): Observable<AdminIncidenciaView[]> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http
      .get<SpringPageResponse<AdminIncidenceResponseDto>>(`${this.BASE_URL}/history`, { params })
      .pipe(
        map((response) => (response.content ?? []).map((item) => this.mapDtoToView(item))),
        tap((incidencias) => this.incidenciasHistorialSignal.set(incidencias)),
      );
  }

  /**
   * Envía la resolución de una incidencia (aprobada o rechazada) al backend.
   *
   * @description
   * Realiza una petición HTTP PATCH para actualizar el estado de una incidencia específica.
   * El componente que llama a este método es responsable de refrescar los datos si es necesario.
   *
   * @param id - El identificador único de la incidencia a gestionar.
   * @param request - Un objeto `AdminIncidenceRequestDto` con el nuevo estado y la respuesta del administrador.
   * @returns Un `Observable<void>` que se completa cuando la operación ha finalizado.
   */
  gestionarIncidencia(id: string, request: AdminIncidenceRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}/${id}/manage`, request);
  }

  private mapDtoToView(dto: AdminIncidenceResponseDto): AdminIncidenciaView {
    return {
      id: dto.id,
      status: this.mapEstado(dto.status),
      created_at: dto.createdAt,
      comment: dto.comment,
      description: dto.comment,
      admin_response: dto.adminResponse,
      profiles: {
        full_name: dto.employeeName,
        avatar_url: dto.avatarUrl,
        job_positions: {
          title: dto.jobPosition?.trim() || 'Sin cargo',
        },
      },
      incidence_types: {
        name: dto.incidenceType,
      },
    };
  }

  private mapEstado(estado: string): string {
    const normalized = (estado || '').toLowerCase();

    if (normalized === 'resolved' || normalized === 'resuelta') {
      return 'Resuelta';
    }

    if (normalized === 'rejected' || normalized === 'rechazada') {
      return 'Rechazada';
    }

    return 'Pendiente';
  }
}
