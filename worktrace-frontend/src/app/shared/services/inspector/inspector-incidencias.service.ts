import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { InspectorIncidenceDto } from '../../models/inspector.model';
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
export class InspectorIncidenciaService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly INCIDENCE_TYPES_URL = `${API_CONFIG.baseUrl}incidence-types`;
  private readonly http = inject(HttpClient);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);

  getIncidencias(
    page: number = 0,
    size: number = 10,
    estado?: string,
    tipoIncidenciaId?: string,
    busqueda?: string,
  ): Observable<SpringPageResponse<InspectorIncidenceDto>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (estado && estado.trim()) {
      params = params.set('status', estado.trim());
    }

    if (tipoIncidenciaId && tipoIncidenciaId.trim()) {
      params = params.set('incidenceTypeId', tipoIncidenciaId.trim());
    }

    if (busqueda && busqueda.trim()) {
      params = params.set('search', busqueda.trim());
    }

    return this.http.get<SpringPageResponse<InspectorIncidenceDto>>(
      `${this.BASE_URL}/incidences`,
      { params },
    );
  }

  getIncidenceTypes(): Observable<IncidenceTypeResponseDto> {
    return this.http.get<IncidenceTypeResponseDto>(this.INCIDENCE_TYPES_URL).pipe(
      tap((response) => {
        this.incidenceTypesSignal.set(response?.types ?? []);
      }),
    );
  }
}
