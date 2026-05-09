import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { StatisticsResponse } from '../../models/time-entry.model';

/**
 * @class WorkerEstadisticasService
 * @description
 * Servicio para la obtención de estadísticas de fichajes para el trabajador autenticado.
 * Permite consultar datos agregados sobre las horas trabajadas y descargar informes
 * en un rango de fechas específico.
 */
@Injectable({
  providedIn: 'root',
})
export class WorkerEstadisticasService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly estadisticasSignal = signal<StatisticsResponse | null>(null);

  /**
   * Obtiene las estadísticas de fichajes del usuario para un rango de fechas.
   *
   * @description
   * Realiza una petición GET para calcular estadísticas como el total de horas trabajadas,
   * el promedio diario y el balance de horas. Actualiza el `estadisticasSignal` con
   * la respuesta. Si no se proveen fechas, el backend calcula sobre un rango por defecto.
   *
   * @param startDate - La fecha de inicio del período (objeto `Date` o string 'YYYY-MM-DD').
   * @param endDate - La fecha de fin del período (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un objeto `StatisticsResponse` con los datos estadísticos.
   */
  getStatistics(
    startDate?: Date | string,
    endDate?: Date | string,
  ): Observable<StatisticsResponse> {
    let params = new HttpParams();

    if (startDate) {
      const startStr = typeof startDate === 'string' ? startDate : this.formatDateToIso(startDate);
      params = params.set('startDate', startStr);
    }
    if (endDate) {
      const endStr = typeof endDate === 'string' ? endDate : this.formatDateToIso(endDate);
      params = params.set('endDate', endStr);
    }

    return this.http
      .get<StatisticsResponse>(`${this.BASE_URL}time-entries/statistics`, { params })
      .pipe(tap((estadisticas) => this.estadisticasSignal.set(estadisticas)));
  }

  /**
   * Genera y descarga un informe de estadísticas en formato PDF.
   *
   * @description
   * Solicita al backend la generación de un informe en PDF con las estadísticas de fichajes
   * del usuario para el rango de fechas especificado.
   *
   * @param startDate - La fecha de inicio del período del informe.
   * @param endDate - La fecha de fin del período del informe.
   * @returns Un `Observable` que emite un `Blob` con el contenido del archivo PDF.
   */
  downloadStatisticsPdf(startDate?: Date | string, endDate?: Date | string): Observable<Blob> {
    let params = new HttpParams();

    if (startDate) {
      const startStr = typeof startDate === 'string' ? startDate : this.formatDateToIso(startDate);
      params = params.set('startDate', startStr);
    }

    if (endDate) {
      const endStr = typeof endDate === 'string' ? endDate : this.formatDateToIso(endDate);
      params = params.set('endDate', endStr);
    }

    return this.http.get(`${this.BASE_URL}time-entries/statistics/export`, {
      params,
      responseType: 'blob',
    });
  }

  private formatDateToIso(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
