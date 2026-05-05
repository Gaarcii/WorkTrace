import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  AdminTimeEntryByDateResponseDto,
  ActiveWorkerDto,
  DailyTimeEntryCountDto,
  TotalHoursTodayResponseDto,
} from '../../models/time-entry.model';
import { AdminIncidenceResponseDto } from '../../models/incidence.model';
import { DepartmentStatDto } from '../../models/profile.model';
import { InspectorRequestDto } from '../../models/inspector.model';

interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}

@Injectable({
  providedIn: 'root',
})
export class AdminHomeService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);

  readonly activeWorkersSignal = signal<ActiveWorkerDto[]>([]);
  readonly totalWorkersSignal = signal<number | null>(null);
  readonly adminIncidenciasSignal = signal<AdminIncidenceResponseDto[]>([]);
  readonly adminIncidenciasTotalSignal = signal<number>(0);
  readonly numFichajesHoySignal = signal<number | null>(null);
  readonly horasHoySignal = signal<TotalHoursTodayResponseDto | null>(null);
  readonly weeklyChartSignal = signal<DailyTimeEntryCountDto[]>([]);
  readonly departmentStatsSignal = signal<DepartmentStatDto[]>([]);

  getActiveWorkers(): Observable<ActiveWorkerDto[]> {
    return this.http
      .get<ActiveWorkerDto[]>(`${this.BASE_URL}time-entries/active-workers`)
      .pipe(tap((workers) => this.activeWorkersSignal.set(workers)));
  }

  obtenerTotalTrabajadores(): Observable<number> {
    return this.http
      .get<number>(`${this.BASE_URL}user/total-workers`)
      .pipe(tap((total) => this.totalWorkersSignal.set(total)));
  }

  obtenerDepartamentos(): Observable<DepartmentStatDto[]> {
    return this.http
      .get<DepartmentStatDto[]>(`${this.BASE_URL}user/departments`)
      .pipe(tap((departments) => this.departmentStatsSignal.set(departments)));
  }

  obtenerIncidenciasAdmin(
    status: string = 'PENDING',
    page: number = 0,
    size: number = 10,
  ): Observable<AdminIncidenceResponseDto[]> {
    const params = new HttpParams().set('status', status).set('page', page).set('size', size);

    return this.http
      .get<SpringPageResponse<AdminIncidenceResponseDto>>(`${this.BASE_URL}incidences/admin`, {
        params,
      })
      .pipe(
        tap((response) => {
          const incidencias = response.content ?? [];
          this.adminIncidenciasSignal.set(incidencias);
          this.adminIncidenciasTotalSignal.set(response.totalElements ?? incidencias.length);
        }),
        map((response) => response.content ?? []),
      );
  }

  obtenerNumFichajesHoy(): Observable<number> {
    return this.http
      .get<number>(`${this.BASE_URL}time-entries/count-today`)
      .pipe(tap((totalFichajes) => this.numFichajesHoySignal.set(totalFichajes)));
  }

  getTotalHoursToday(): Observable<TotalHoursTodayResponseDto> {
    return this.http
      .get<TotalHoursTodayResponseDto>(`${this.BASE_URL}time-entries/hours-today`)
      .pipe(tap((horas) => this.horasHoySignal.set(horas)));
  }

  getWeeklyChart(
    startDate: Date | string,
    endDate: Date | string,
  ): Observable<DailyTimeEntryCountDto[]> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http
      .get<DailyTimeEntryCountDto[]>(`${this.BASE_URL}time-entries/weekly-chart`, { params })
      .pipe(tap((weeklyChart) => this.weeklyChartSignal.set(weeklyChart)));
  }

  obtenerWeeklyChart(
    fechaInicio: Date | string,
    fechaFin: Date | string,
  ): Observable<DailyTimeEntryCountDto[]> {
    return this.getWeeklyChart(fechaInicio, fechaFin);
  }

  getTimeEntriesByDate(date: Date | string): Observable<AdminTimeEntryByDateResponseDto[]> {
    const params = new HttpParams().set('date', this.toIsoDate(date));
    return this.http.get<AdminTimeEntryByDateResponseDto[]>(
      `${this.BASE_URL}time-entries/admin/by-date`,
      {
        params,
      },
    );
  }

  exportReportPdf(startDate: Date | string, endDate: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http.get(`${this.BASE_URL}time-entries/admin/export/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  exportarInformeEmpresaExcel(
    fechaInicio: Date | string,
    fechaFin: Date | string,
  ): Observable<Blob> {
    return this.exportReportExcel(fechaInicio, fechaFin);
  }

  exportReportExcel(startDate: Date | string, endDate: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http.get(`${this.BASE_URL}time-entries/admin/export/excel`, {
      params,
      responseType: 'blob',
    });
  }

  registerInspector(data: InspectorRequestDto): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.BASE_URL}auth/register-inspector`, data);
  }

  private toIsoDate(value: Date | string): string {
    if (typeof value === 'string') {
      return value;
    }

    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
