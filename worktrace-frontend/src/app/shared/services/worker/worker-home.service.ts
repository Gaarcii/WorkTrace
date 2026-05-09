import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';

import {
  DailySummaryResponse,
  TimeEntryRequest,
  TimeEntryResponse,
} from '../../models/time-entry.model';

/**
 * @class WorkerHomeService
 * @description
 * Servicio para la página principal del trabajador. Gestiona la obtención del resumen
 * diario de la jornada y la ejecución de las acciones de fichaje.
 */
@Injectable({
  providedIn: 'root',
})
export class WorkerHomeService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly resumenSignal = signal<DailySummaryResponse | null>(null);

  /**
   * Obtiene el resumen diario de la jornada del trabajador autenticado.
   *
   * @description
   * Realiza una petición GET para obtener el estado del último fichaje y el total de
   * horas trabajadas en el día. Actualiza el `resumenSignal` con la respuesta.
   *
   * @returns Un `Observable` que emite un objeto `DailySummaryResponse` con el resumen.
   */
  getDailySummary(): Observable<DailySummaryResponse> {
    return this.http.get<DailySummaryResponse>(`${this.BASE_URL}time-entries/daily-summary`).pipe(
      tap((resumen) => {
        this.resumenSignal.set(resumen);
      }),
    );
  }

  /**
   * Registra un evento de entrada (inicio de jornada o fin de pausa).
   *
   * @description
   * Realiza una petición POST para registrar el fichaje. Después de una operación exitosa,
   * actualiza automáticamente el resumen diario para reflejar el nuevo estado.
   *
   * @param request - Un objeto `TimeEntryRequest` que puede contener la ubicación y el tipo de fichaje.
   * @returns Un `Observable` que emite un objeto `TimeEntryResponse` con los detalles del fichaje creado.
   */
  clockIn(request: TimeEntryRequest): Observable<TimeEntryResponse> {
    return this.http.post<TimeEntryResponse>(`${this.BASE_URL}time-entries/clock-in`, request).pipe(
      tap(() => {
        this.getDailySummary().subscribe();
      }),
    );
  }
}
