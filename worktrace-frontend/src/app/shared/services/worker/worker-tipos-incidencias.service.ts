import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  IncidenceTypeProjection,
  IncidenceTypeResponseDto,
} from '../../models/incidence-type.model';

/**
 * @class WorkerIncidenceTypesService
 * @description
 * Servicio encargado de obtener los tipos de incidencia disponibles que un trabajador
 * puede seleccionar al registrar una nueva incidencia.
 */
@Injectable({
  providedIn: 'root',
})
export class WorkerIncidenceTypesService {
  private readonly http = inject(HttpClient);

  private readonly BASE_URL = API_CONFIG.baseUrl + 'incidence-types';

  readonly tiposSignal = signal<IncidenceTypeProjection[]>([]);

  /**
   * Obtiene la lista de todos los tipos de incidencia disponibles en el sistema.
   *
   * @description
   * Realiza una petición GET para obtener los tipos de incidencia y, si la operación es exitosa,
   * actualiza el `tiposSignal` con la lista de tipos recibida.
   *
   * @returns Un `Observable` que emite un objeto `IncidenceTypeResponseDto` que contiene la lista de tipos.
   */
  obtenerTipos(): Observable<IncidenceTypeResponseDto> {
    return this.http.get<IncidenceTypeResponseDto>(this.BASE_URL).pipe(
      tap((response) => {
        if (response && response.types) {
          this.tiposSignal.set(response.types);
        }
      }),
    );
  }
}
