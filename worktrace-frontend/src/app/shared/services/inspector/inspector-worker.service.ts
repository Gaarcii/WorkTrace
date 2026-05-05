import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  EmployeeDetailDto,
  EmployeeDto,
  InspectorAuditDetailDto,
  InspectorAuditDto,
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
  ): Observable<SpringPageResponse<EmployeeDto>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<SpringPageResponse<EmployeeDto>>(`${this.BASE_URL}/employees`, {
      params,
    });
  }

  getEmpleadoDetalle(id: string): Observable<EmployeeDetailDto> {
    return this.http.get<EmployeeDetailDto>(`${this.BASE_URL}/employees/${id}`);
  }
}
