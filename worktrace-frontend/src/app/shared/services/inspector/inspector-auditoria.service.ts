import {inject, Injectable, signal} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {API_CONFIG} from '../../../core/api/api.config';
import {EmployeeDetailDto, InspectorAuditDetailDto, InspectorAuditDto,} from '../../models/inspector.model';
import {IncidenceTypeProjection} from '../../models/incidence-type.model';

interface SpringPageResponse<T> {
  content: T[];
  page?: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

/**
 * @class InspectorAuditoriaService
 * @description
 * Servicio destinado al rol de Inspector. Proporciona los métodos necesarios para
 * consultar el registro de auditoría del sistema y los detalles de los empleados,
 * permitiendo realizar un seguimiento de las acciones importantes.
 */
@Injectable({
  providedIn: 'root',
})
export class InspectorAuditoriaService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly http = inject(HttpClient);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);

  /**
   * Obtiene un listado paginado y filtrable de los registros de auditoría.
   *
   * @description
   * Permite consultar las acciones registradas en el sistema, con la posibilidad de
   * filtrar por tipo de acción y por un rango de fechas.
   *
   * @param page - El número de página a solicitar (basado en 0).
   * @param size - El tamaño de la página.
   * @param action - El tipo de acción a filtrar (ej. 'USER_LOGIN', 'INCIDENCE_REJECTED').
   * @param startDate - La fecha de inicio para el filtro de rango (string 'YYYY-MM-DD' o Date).
   * @param endDate - La fecha de fin para el filtro de rango (string 'YYYY-MM-DD' o Date).
   * @returns Un `Observable` que emite una respuesta paginada `SpringPageResponse<InspectorAuditDto>`.
   */
  getAuditorias(
    page = 0,
    size = 10,
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

    return this.http.get<SpringPageResponse<InspectorAuditDto>>(`${this.BASE_URL}/audits`, {
      params,
    });
  }

  /**
   * Obtiene el detalle completo de un registro de auditoría específico.
   *
   * @param id - El identificador único del registro de auditoría.
   * @returns Un `Observable` que emite un `InspectorAuditDetailDto` con los detalles del evento.
   */
  getAuditoriaDetalle(id: string): Observable<InspectorAuditDetailDto> {
    return this.http.get<InspectorAuditDetailDto>(`${this.BASE_URL}/audits/${id}`);
  }

  /**
   * Obtiene los detalles de un empleado, incluyendo su información personal y laboral.
   *
   * @param id - El identificador único del empleado.
   * @returns Un `Observable` que emite un `EmployeeDetailDto` con los datos del empleado.
   */
  getEmpleadoDetalle(id: string): Observable<EmployeeDetailDto> {
    return this.http.get<EmployeeDetailDto>(`${this.BASE_URL}/employees/${id}`);
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
