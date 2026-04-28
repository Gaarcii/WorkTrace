import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { EmpleadoDetalleDto, EmpleadoDto } from '../../models/inspector.model';

interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}

@Injectable({
  providedIn: 'root',
})
export class InspectorWorkerService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly http = inject(HttpClient);

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
}
