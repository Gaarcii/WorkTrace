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

/**
 * @class InspectorWorkerService
 * @description
 * Servicio para el rol de Inspector, centrado en la consulta de información sobre los empleados.
 * Permite obtener listados paginados y con búsqueda de empleados, así como los detalles
 * específicos de un trabajador.
 */
@Injectable({
  providedIn: 'root',
})
export class InspectorWorkerService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly INCIDENCE_TYPES_URL = `${API_CONFIG.baseUrl}incidence/types`;
  private readonly http = inject(HttpClient);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);

  /**
   * Obtiene una lista paginada de empleados, con opción de búsqueda.
   *
   * @description
   * Realiza una petición GET para obtener un listado de empleados. Permite una búsqueda
   * de texto libre que puede aplicarse sobre el nombre, apellidos o DNI del empleado.
   *
   * @param page - El número de página a solicitar (basado en 0).
   * @param size - El número de elementos por página.
   * @param search - Un término de búsqueda de texto libre.
   * @returns Un `Observable` que emite una respuesta paginada `SpringPageResponse<EmployeeDto>`.
   */
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

  /**
   * Obtiene los detalles completos de un empleado específico.
   *
   * @description
   * Realiza una petición GET para obtener toda la información relevante de un empleado,
   * incluyendo datos personales, laborales y de contacto.
   *
   * @param id - El identificador único del empleado.
   * @returns Un `Observable` que emite un objeto `EmployeeDetailDto` con los datos del empleado.
   */
  getEmpleadoDetalle(id: string): Observable<EmployeeDetailDto> {
    return this.http.get<EmployeeDetailDto>(`${this.BASE_URL}/employees/${id}`);
  }
}
