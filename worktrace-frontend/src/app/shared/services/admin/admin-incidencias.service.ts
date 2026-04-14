import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  AdminIncidenceManageRequestDto,
  AdminIncidenceResponseDto,
} from '../../models/incidence.model';
import { AdminIncidenciaView } from '../../../features/admin/incidencias/admin-incidencias.types';

interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}

@Injectable({
  providedIn: 'root',
})
export class AdminIncidenciasService {
  private readonly http = inject(HttpClient);

  private readonly BASE_URL = API_CONFIG.baseUrl + 'incidence';

  readonly incidenciasPendientesSignal = signal<AdminIncidenciaView[]>([]);
  readonly incidenciasHistorialSignal = signal<AdminIncidenciaView[]>([]);

  obtenerPendientes(page: number = 0, size: number = 100): Observable<AdminIncidenciaView[]> {
    const params = new HttpParams().set('status', 'PENDING').set('page', page).set('size', size);

    return this.http
      .get<SpringPageResponse<AdminIncidenceResponseDto>>(`${this.BASE_URL}/admin`, { params })
      .pipe(
        map((response) => (response.content ?? []).map((item) => this.mapDtoToView(item))),
        tap((incidencias) => this.incidenciasPendientesSignal.set(incidencias)),
      );
  }

  obtenerHistorial(page: number = 0, size: number = 100): Observable<AdminIncidenciaView[]> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http
      .get<SpringPageResponse<AdminIncidenceResponseDto>>(`${this.BASE_URL}/history`, { params })
      .pipe(
        map((response) => (response.content ?? []).map((item) => this.mapDtoToView(item))),
        tap((incidencias) => this.incidenciasHistorialSignal.set(incidencias)),
      );
  }

  gestionarIncidencia(id: string, request: AdminIncidenceManageRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}/${id}/manage`, request);
  }

  private mapDtoToView(dto: AdminIncidenceResponseDto): AdminIncidenciaView {
    return {
      id: dto.id,
      status: this.mapEstado(dto.estado),
      created_at: dto.creacion,
      comment: dto.comentario,
      description: dto.comentario,
      admin_response: dto.adminResponse,
      profiles: {
        full_name: dto.nombreTrabajador,
        avatar_url: dto.avatarUrl,
        job_positions: {
          title: dto.puestoTrabajo?.trim() || 'Sin cargo',
        },
      },
      incidence_types: {
        name: dto.tipoIncidencia,
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
