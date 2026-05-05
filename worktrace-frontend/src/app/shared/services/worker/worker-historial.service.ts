import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { HistoryResponse } from '../../models/time-entry.model';

@Injectable({
  providedIn: 'root',
})
export class WorkerHistoryService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly historialSignal = signal<HistoryResponse | null>(null);

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
