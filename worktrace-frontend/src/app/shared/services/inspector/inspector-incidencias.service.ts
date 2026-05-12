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

/**
 * @class InspectorIncidenciaService
 * @description
 * Servicio para el rol de Inspector, enfocado en la consulta y filtrado de incidencias.
 * Permite obtener una vista completa y paginada de todas las incidencias del sistema,
 * con capacidades avanzadas de búsqueda y filtrado.
 */
@Injectable({
  providedIn: 'root',
})
export class InspectorIncidenciaService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly INCIDENCE_TYPES_URL = `${API_CONFIG.baseUrl}incidence-types`;
  private readonly http = inject(HttpClient);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);

  /**
   * Obtiene una lista paginada y filtrable de todas las incidencias del sistema.
   *
   * @description
   * Realiza una petición GET que permite filtrar incidencias por estado, tipo y un término
   * de búsqueda general que puede aplicar sobre el nombre del empleado o el comentario.
   *
   * @param page - El número de página a solicitar (basado en 0).
   * @param size - El número de elementos por página.
   * @param estado - El estado por el cual filtrar las incidencias (ej. 'PENDING', 'RESOLVED').
   * @param tipoIncidenciaId - El ID del tipo de incidencia para acotar la búsqueda.
   * @param busqueda - Un término de búsqueda de texto libre.
   * @returns Un `Observable` que emite una respuesta paginada `SpringPageResponse<InspectorIncidenceDto>`.
   */
  getIncidencias(
    page = 0,
    size = 10,
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

  /**
   * Obtiene la lista completa de tipos de incidencia disponibles en el sistema.
   *
   * @description
   * Realiza una petición GET para obtener todos los tipos de incidencia y actualiza
   * el `incidenceTypesSignal` para que los componentes puedan reaccionar a estos datos.
   *
   * @returns Un `Observable` que emite un `IncidenceTypeResponseDto` conteniendo la lista de tipos.
   */
  getIncidenceTypes(): Observable<IncidenceTypeResponseDto> {
    return this.http.get<IncidenceTypeResponseDto>(this.INCIDENCE_TYPES_URL).pipe(
      tap((response) => {
        this.incidenceTypesSignal.set(response?.types ?? []);
      }),
    );
  }
}
