import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  EmpleadoDetalleDto,
  InspectorAuditDetailDto,
  InspectorAuditDto,
} from '../../models/inspector.model';
import { IncidenceTypeProjection } from '../../models/incidence-type.model';

interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}

@Injectable({
  providedIn: 'root',
})
export class InspectorAuditoriaService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly INCIDENCE_TYPES_URL = `${API_CONFIG.baseUrl}incidence/types`;
  private readonly http = inject(HttpClient);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);
  getAuditorias(
    page: number = 0,
    size: number = 10,
    action?: string,
    startDate?: string | Date,
    endDate?: string | Date,
  ): Observable<SpringPageResponse<InspectorAuditDto>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (action && action.trim()) {
      params = params.set('action', action.trim());
    }

    const normalizedStartDate = this.normalizeDateParam(startDate);
    const normalizedEndDate = this.normalizeDateParam(endDate);

    if (normalizedStartDate) {
      params = params.set('startDate', normalizedStartDate);
    }

    if (normalizedEndDate) {
      params = params.set('endDate', normalizedEndDate);
    }

    return this.http.get<SpringPageResponse<InspectorAuditDto>>(`${this.BASE_URL}/auditoria`, {
      params,
    });
  }

  getAuditoriaDetalle(id: string): Observable<InspectorAuditDetailDto> {
    return this.http.get<InspectorAuditDetailDto>(`${this.BASE_URL}/auditoria/${id}`);
  }

  getEmpleadoDetalle(id: string): Observable<EmpleadoDetalleDto> {
    return this.http.get<EmpleadoDetalleDto>(`${this.BASE_URL}/empleados/${id}`);
  }

  private normalizeDateParam(value?: string | Date): string | undefined {
    if (!value) {
      return undefined;
    }

    if (value instanceof Date) {
      return Number.isNaN(value.getTime()) ? undefined : value.toISOString().slice(0, 10);
    }

    const trimmedValue = value.trim();
    return trimmedValue ? trimmedValue : undefined;
  }
}
