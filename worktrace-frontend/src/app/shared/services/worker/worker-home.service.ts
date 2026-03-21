import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';

import {
  ResumenResponse,
  TimeEntryRequest,
  TimeEntryResponse,
} from '../../models/time-entry.model';
@Injectable({
  providedIn: 'root',
})
export class WorkerHomeService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly resumenSignal = signal<ResumenResponse | null>(null);

  obtenerResumenDiario(): Observable<ResumenResponse> {
    return this.http.get<ResumenResponse>(`${this.BASE_URL}time-entries/resumenDiario`).pipe(
      tap((resumen) => {
        this.resumenSignal.set(resumen);
      }),
    );
  }

  fichar(request: TimeEntryRequest): Observable<TimeEntryResponse> {
    return this.http.post<TimeEntryResponse>(`${this.BASE_URL}time-entries/fichar`, request).pipe(
      tap(() => {
        this.obtenerResumenDiario().subscribe();
      }),
    );
  }
}
