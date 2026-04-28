import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  EmpleadoDetalleDto,
  EmpleadoDto,
  InspectorDailyClosureDto,
  InspectorIncidenceDto,
} from '../../models/inspector.model';
import {
  IncidenceTypeProjection,
  IncidenceTypeResponseDto,
} from '../../models/incidence-type.model';

interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}

@Injectable({
  providedIn: 'root',
})
export class InspectorWorkerService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly INCIDENCE_TYPES_URL = `${API_CONFIG.baseUrl}incidence/types`;
  private readonly http = inject(HttpClient);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);

  getEmpleados(
    page: number = 0,
    size: number = 10,
    search?: string,
  ): Observable<SpringPageResponse<EmpleadoDto>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<SpringPageResponse<EmpleadoDto>>(`${this.BASE_URL}/empleados`, {
      params,
    });
  }

  getEmpleadoDetalle(id: string): Observable<EmpleadoDetalleDto> {
    return this.http.get<EmpleadoDetalleDto>(`${this.BASE_URL}/empleados/${id}`);
  }

  getIncidencias(
    page: number = 0,
    size: number = 10,
    estado?: string,
    tipoIncidenciaId?: string,
    busqueda?: string,
  ): Observable<SpringPageResponse<InspectorIncidenceDto>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (estado && estado.trim()) {
      params = params.set('estado', estado.trim());
    }

    if (tipoIncidenciaId && tipoIncidenciaId.trim()) {
      params = params.set('tipoIncidenciaId', tipoIncidenciaId.trim());
    }

    if (busqueda && busqueda.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }

    return this.http.get<SpringPageResponse<InspectorIncidenceDto>>(
      `${this.BASE_URL}/incidencias`,
      { params },
    );
  }

  getIncidenceTypes(): Observable<IncidenceTypeResponseDto> {
    return this.http.get<IncidenceTypeResponseDto>(this.INCIDENCE_TYPES_URL).pipe(
      tap((response) => {
        this.incidenceTypesSignal.set(response?.tipos ?? []);
      }),
    );
  }

  getRegistrosDiarios(
    page: number = 0,
    size: number = 10,
    startDate?: string | Date,
    endDate?: string | Date,
  ): Observable<SpringPageResponse<InspectorDailyClosureDto>> {
    let params = new HttpParams().set('page', page).set('size', size);

    const normalizedStartDate = this.normalizeDateParam(startDate);
    const normalizedEndDate = this.normalizeDateParam(endDate);

    if (normalizedStartDate) {
      params = params.set('startDate', normalizedStartDate);
    }

    if (normalizedEndDate) {
      params = params.set('endDate', normalizedEndDate);
    }

    return this.http.get<SpringPageResponse<InspectorDailyClosureDto>>(
      `${this.BASE_URL}/registros-diarios`,
      { params },
    );
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
