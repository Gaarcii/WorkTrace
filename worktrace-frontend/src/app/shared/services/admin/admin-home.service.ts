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

/**
 * @class AdminHomeService
 * @description
 * Servicio dedicado a proveer los datos necesarios para el dashboard principal del administrador.
 * Gestiona la obtención de estadísticas clave, datos en tiempo real como trabajadores activos,
 * incidencias pendientes, y la generación de informes.
 */
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

  /**
   * Obtiene la lista de trabajadores que tienen una sesión de trabajo activa en este momento.
   *
   * @description
   * Realiza una petición GET y actualiza el `activeWorkersSignal` con el resultado.
   *
   * @returns Un `Observable` que emite un array de `ActiveWorkerDto`.
   */
  getActiveWorkers(): Observable<ActiveWorkerDto[]> {
    return this.http
      .get<ActiveWorkerDto[]>(`${this.BASE_URL}time-entries/active-workers`)
      .pipe(tap((workers) => this.activeWorkersSignal.set(workers)));
  }

  /**
   * Obtiene el número total de trabajadores registrados en la empresa.
   *
   * @description
   * Realiza una petición GET y actualiza el `totalWorkersSignal` con el resultado.
   *
   * @returns Un `Observable` que emite el número total de trabajadores.
   */
  obtenerTotalTrabajadores(): Observable<number> {
    return this.http
      .get<number>(`${this.BASE_URL}user/total-workers`)
      .pipe(tap((total) => this.totalWorkersSignal.set(total)));
  }

  /**
   * Obtiene las estadísticas de distribución de trabajadores por departamento.
   *
   * @description
   * Realiza una petición GET y actualiza el `departmentStatsSignal` con los datos para gráficos.
   *
   * @returns Un `Observable` que emite un array de `DepartmentStatDto`.
   */
  obtenerDepartamentos(): Observable<DepartmentStatDto[]> {
    return this.http
      .get<DepartmentStatDto[]>(`${this.BASE_URL}user/departments`)
      .pipe(tap((departments) => this.departmentStatsSignal.set(departments)));
  }

  /**
   * Obtiene una lista paginada de incidencias para la vista de administrador.
   *
   * @description
   * Permite filtrar por estado y paginar los resultados. Actualiza `adminIncidenciasSignal`
   * con el contenido de la página actual y `adminIncidenciasTotalSignal` con el total de elementos.
   *
   * @param status - El estado de las incidencias a obtener (ej. 'PENDING'). Por defecto es 'PENDING'.
   * @param page - El número de página a solicitar (basado en 0). Por defecto es 0.
   * @param size - El tamaño de la página. Por defecto es 10.
   * @returns Un `Observable` que emite un array con las `AdminIncidenceResponseDto` de la página solicitada.
   */
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

  /**
   * Obtiene el número total de fichajes (entradas y salidas) realizados en el día de hoy.
   *
   * @description
   * Realiza una petición GET y actualiza `numFichajesHoySignal` con el valor.
   *
   * @returns Un `Observable` que emite el número total de fichajes de hoy.
   */
  obtenerNumFichajesHoy(): Observable<number> {
    return this.http
      .get<number>(`${this.BASE_URL}time-entries/count-today`)
      .pipe(tap((totalFichajes) => this.numFichajesHoySignal.set(totalFichajes)));
  }

  /**
   * Obtiene el total de horas computadas y el promedio por trabajador para el día de hoy.
   *
   * @description
   * Realiza una petición GET y actualiza `horasHoySignal` con el resultado.
   *
   * @returns Un `Observable` que emite un objeto `TotalHoursTodayResponseDto`.
   */
  getTotalHoursToday(): Observable<TotalHoursTodayResponseDto> {
    return this.http
      .get<TotalHoursTodayResponseDto>(`${this.BASE_URL}time-entries/hours-today`)
      .pipe(tap((horas) => this.horasHoySignal.set(horas)));
  }

  /**
   * Obtiene los datos agregados por día para construir un gráfico de actividad semanal.
   *
   * @description
   * Solicita el recuento de fichajes para cada día en el rango de fechas especificado y
   * actualiza `weeklyChartSignal` con el resultado.
   *
   * @param startDate - La fecha de inicio del rango (objeto `Date` o string 'YYYY-MM-DD').
   * @param endDate - La fecha de fin del rango (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un array de `DailyTimeEntryCountDto`.
   */
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

  /**
   * Obtiene los fichajes de un día específico para la tabla de visualización del administrador.
   *
   * @param date - La fecha para la cual se solicitan los fichajes (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un array de `AdminTimeEntryByDateResponseDto`.
   */
  getTimeEntriesByDate(date: Date | string): Observable<AdminTimeEntryByDateResponseDto[]> {
    const params = new HttpParams().set('date', this.toIsoDate(date));
    return this.http.get<AdminTimeEntryByDateResponseDto[]>(
      `${this.BASE_URL}time-entries/admin/by-date`,
      {
        params,
      },
    );
  }

  /**
   * Genera y descarga un informe de fichajes en formato PDF para un rango de fechas.
   *
   * @param startDate - La fecha de inicio del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @param endDate - La fecha de fin del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un `Blob` con el contenido del archivo PDF.
   */
  exportReportPdf(startDate: Date | string, endDate: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http.get(`${this.BASE_URL}time-entries/admin/export/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Genera y descarga un informe de fichajes en formato Excel para un rango de fechas.
   *
   * @param startDate - La fecha de inicio del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @param endDate - La fecha de fin del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un `Blob` con el contenido del archivo Excel.
   */
  exportReportExcel(startDate: Date | string, endDate: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http.get(`${this.BASE_URL}time-entries/admin/export/excel`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Registra una nueva cuenta de usuario con el rol de Inspector.
   *
   * @param data - Objeto `InspectorRequestDto` con los datos del nuevo inspector.
   * @returns Un `Observable` que emite un objeto con un mensaje de confirmación.
   */
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
