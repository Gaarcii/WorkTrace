import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { InspectorDailyClosureDto } from '../../models/inspector.model';
import { IncidenceTypeProjection } from '../../models/incidence-type.model';

interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}

/**
 * @class InspectorRegistroDiarioService
 * @description
 * Servicio para el rol de Inspector, enfocado en la consulta de los registros de cierre diario.
 * Permite obtener una vista paginada y filtrable por fechas de los resúmenes de jornada laboral.
 */
@Injectable({
  providedIn: 'root',
})
export class InspectorRegistroDiarioService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly http = inject(HttpClient);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);

  /**
   * Obtiene una lista paginada y filtrable por fechas de los registros de cierre diario.
   *
   * @description
   * Realiza una petición GET para consultar los cierres diarios, que resumen la actividad
   * laboral de cada día. Permite acotar la búsqueda a un rango de fechas específico.
   *
   * @param page - El número de página a solicitar (basado en 0).
   * @param size - El número de elementos por página.
   * @param startDate - La fecha de inicio para el filtro (string 'YYYY-MM-DD' o Date).
   * @param endDate - La fecha de fin para el filtro (string 'YYYY-MM-DD' o Date).
   * @returns Un `Observable` que emite una respuesta paginada `SpringPageResponse<InspectorDailyClosureDto>`.
   */
  getRegistrosDiarios(
    page = 0,
    size = 10,
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
      `${this.BASE_URL}/daily-closures`,
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
