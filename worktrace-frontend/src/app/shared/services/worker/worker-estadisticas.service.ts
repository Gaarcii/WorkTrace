import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { StatisticsResponse } from '../../models/time-entry.model';

@Injectable({
  providedIn: 'root',
})
export class WorkerEstadisticasService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly estadisticasSignal = signal<StatisticsResponse | null>(null);

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
