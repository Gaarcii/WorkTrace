import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { EstadisticasResponse } from '../../models/time-entry.model';

@Injectable({
  providedIn: 'root',
})
export class WorkerEstadisticasService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly estadisticasSignal = signal<EstadisticasResponse | null>(null);

  obtenerEstadisticas(
    fechaInicio?: Date | string,
    fechaFin?: Date | string,
  ): Observable<EstadisticasResponse> {
    let params = new HttpParams();

    if (fechaInicio) {
      const inicioStr =
        typeof fechaInicio === 'string' ? fechaInicio : this.formatDateToIso(fechaInicio);
      params = params.set('fechaInicio', inicioStr);
    }
    if (fechaFin) {
      const finStr = typeof fechaFin === 'string' ? fechaFin : this.formatDateToIso(fechaFin);
      params = params.set('fechaFin', finStr);
    }

    return this.http
      .get<EstadisticasResponse>(`${this.BASE_URL}time-entries/estadisticas`, { params })
      .pipe(tap((estadisticas) => this.estadisticasSignal.set(estadisticas)));
  }

  descargarInformePdf(fechaInicio?: Date | string, fechaFin?: Date | string): Observable<Blob> {
    let params = new HttpParams();

    if (fechaInicio) {
      const inicioStr =
        typeof fechaInicio === 'string' ? fechaInicio : this.formatDateToIso(fechaInicio);
      params = params.set('fechaInicio', inicioStr);
    }

    if (fechaFin) {
      const finStr = typeof fechaFin === 'string' ? fechaFin : this.formatDateToIso(fechaFin);
      params = params.set('fechaFin', finStr);
    }

    return this.http.get(`${this.BASE_URL}time-entries/estadisticas/exportar`, {
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
