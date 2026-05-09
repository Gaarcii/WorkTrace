import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  WorkerIncidenceRequestDto,
  WorkerIncidenceResponseDto,
} from '../../models/incidence.model';

/**
 * @class WorkerIncidenciasService
 * @description
 * Servicio para la gestión de incidencias del trabajador autenticado.
 * Permite al usuario consultar su historial de incidencias y registrar nuevas.
 */
@Injectable({
  providedIn: 'root',
})
export class WorkerIncidenciasService {
  private readonly http = inject(HttpClient);

  private readonly BASE_URL = API_CONFIG.baseUrl + 'incidences';

  readonly incidenciasSignal = signal<WorkerIncidenceResponseDto[]>([]);

  /**
   * Obtiene el historial de incidencias del trabajador autenticado.
   *
   * @description
   * Realiza una petición GET para obtener todas las incidencias registradas por el usuario
   * y actualiza el `incidenciasSignal` con la respuesta.
   *
   * @returns Un `Observable` que emite un array de `WorkerIncidenceResponseDto`.
   */
  obtenerMisIncidencias(): Observable<WorkerIncidenceResponseDto[]> {
    return this.http
      .get<WorkerIncidenceResponseDto[]>(this.BASE_URL)
      .pipe(tap((incidencias) => this.incidenciasSignal.set(incidencias)));
  }

  /**
   * Registra una nueva incidencia para el trabajador autenticado.
   *
   * @description
   * Realiza una petición POST para crear una nueva incidencia. Si la operación es exitosa,
   * la nueva incidencia se añade al principio de la lista en `incidenciasSignal` para
   * una actualización instantánea de la UI.
   *
   * @param request - Un objeto `WorkerIncidenceRequestDto` con los detalles de la incidencia.
   * @returns Un `Observable` que emite la `WorkerIncidenceResponseDto` de la incidencia recién creada.
   */
  crearIncidencia(request: WorkerIncidenceRequestDto): Observable<WorkerIncidenceResponseDto> {
    return this.http.post<WorkerIncidenceResponseDto>(this.BASE_URL, request).pipe(
      tap((nuevaIncidencia) => {
        this.incidenciasSignal.update((actuales) => [nuevaIncidencia, ...actuales]);
      }),
    );
  }
}
