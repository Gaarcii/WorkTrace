import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { HistorialResponse } from '../../models/time-entry.model';

@Injectable({
  providedIn: 'root',
})
export class WorkerHistoryService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly historialSignal = signal<HistorialResponse | null>(null);

  obtenerHistorial(fecha: Date | string): Observable<HistorialResponse> {

    const fechaString = typeof fecha === 'string' ? fecha : this.formatDateToIso(fecha);

    const params = new HttpParams().set('fecha', fechaString);

    return this.http
      .get<HistorialResponse>(`${this.BASE_URL}time-entries/historial`, { params })
      .pipe(
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
