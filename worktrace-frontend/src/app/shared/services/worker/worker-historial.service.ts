import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { HistoryResponse } from '../../models/time-entry.model';

/**
 * @class WorkerHistoryService
 * @description
 * Servicio para obtener el historial de fichajes de un trabajador en una fecha específica.
 * Proporciona los datos detallados de la jornada laboral de un día concreto.
 */
@Injectable({
  providedIn: 'root',
})
export class WorkerHistoryService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly historialSignal = signal<HistoryResponse | null>(null);

  /**
   * Obtiene el historial de fichajes y el resumen de la jornada para una fecha específica.
   *
   * @description
   * Realiza una petición GET para obtener todos los eventos de fichaje (entradas y salidas)
   * de un día concreto, así como el total de horas computadas para esa jornada.
   * Actualiza el `historialSignal` con la respuesta.
   *
   * @param date - La fecha para la cual se solicita el historial (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un objeto `HistoryResponse` con los detalles de la jornada.
   */
  getHistoryByDate(date: Date | string): Observable<HistoryResponse> {
    const dateString = typeof date === 'string' ? date : this.formatDateToIso(date);
    const params = new HttpParams().set('date', dateString);

    return this.http.get<HistoryResponse>(`${this.BASE_URL}time-entries/history`, { params }).pipe(
      tap((historial) => {
        this.historialSignal.set(historial);
      }),
    );
  }

  private formatDateToIso(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
