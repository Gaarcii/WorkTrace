import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';

import {
  DailySummaryResponse,
  TimeEntryRequest,
  TimeEntryResponse,
} from '../../models/time-entry.model';
@Injectable({
  providedIn: 'root',
})
export class WorkerHomeService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);
  readonly resumenSignal = signal<DailySummaryResponse | null>(null);

  getDailySummary(): Observable<DailySummaryResponse> {
    return this.http.get<DailySummaryResponse>(`${this.BASE_URL}time-entries/daily-summary`).pipe(
      tap((resumen) => {
        this.resumenSignal.set(resumen);
      }),
    );
  }

  clockIn(request: TimeEntryRequest): Observable<TimeEntryResponse> {
    return this.http.post<TimeEntryResponse>(`${this.BASE_URL}time-entries/clock-in`, request).pipe(
      tap(() => {
        this.getDailySummary().subscribe();
      }),
    );
  }
}
