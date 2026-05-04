import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  AdminFichajeDiaDto,
  ActiveWorkerDto,
  DailyFichajeCountDto,
  HorasTrabajadasHoyResponseDto,
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
  readonly horasHoySignal = signal<HorasTrabajadasHoyResponseDto | null>(null);
  readonly weeklyChartSignal = signal<DailyFichajeCountDto[]>([]);
  readonly departmentStatsSignal = signal<DepartmentStatDto[]>([]);

  obtenerTrabajadoresActivos(): Observable<ActiveWorkerDto[]> {
    return this.http
      .get<ActiveWorkerDto[]>(`${this.BASE_URL}time-entries/activeWorkers`)
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
      .get<SpringPageResponse<AdminIncidenceResponseDto>>(`${this.BASE_URL}incidence/admin`, {
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
      .get<number>(`${this.BASE_URL}time-entries/numFichajes`)
      .pipe(tap((totalFichajes) => this.numFichajesHoySignal.set(totalFichajes)));
  }

  obtenerHorasHoy(): Observable<HorasTrabajadasHoyResponseDto> {
    return this.http
      .get<HorasTrabajadasHoyResponseDto>(`${this.BASE_URL}time-entries/horasHoy`)
      .pipe(tap((horas) => this.horasHoySignal.set(horas)));
  }

  obtenerWeeklyChart(
    fechaInicio: Date | string,
    fechaFin: Date | string,
  ): Observable<DailyFichajeCountDto[]> {
    const params = new HttpParams()
      .set('fechaInicio', this.toIsoDate(fechaInicio))
      .set('fechaFin', this.toIsoDate(fechaFin));

    return this.http
      .get<DailyFichajeCountDto[]>(`${this.BASE_URL}time-entries/weeklyChart`, { params })
      .pipe(tap((weeklyChart) => this.weeklyChartSignal.set(weeklyChart)));
  }

  obtenerFichajesPorDia(fecha: Date | string): Observable<AdminFichajeDiaDto[]> {
    const params = new HttpParams().set('fecha', this.toIsoDate(fecha));
    return this.http.get<AdminFichajeDiaDto[]>(`${this.BASE_URL}time-entries/admin/by-day`, {
      params,
    });
  }

  exportarInformeEmpresaPdf(fechaInicio: Date | string, fechaFin: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('fechaInicio', this.toIsoDate(fechaInicio))
      .set('fechaFin', this.toIsoDate(fechaFin));

    return this.http.get(`${this.BASE_URL}time-entries/admin/export/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  exportarInformeEmpresaExcel(
    fechaInicio: Date | string,
    fechaFin: Date | string,
  ): Observable<Blob> {
    const params = new HttpParams()
      .set('fechaInicio', this.toIsoDate(fechaInicio))
      .set('fechaFin', this.toIsoDate(fechaFin));

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
