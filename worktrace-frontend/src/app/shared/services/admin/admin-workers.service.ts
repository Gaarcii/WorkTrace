import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  CreateEmployeeRequestDto,
  EditEmployeeWorkDataRequestDto,
  EmployeeResponseDto,
  JobPositionResponseDto,
  JobPositionUiDto,
  SpringPageResponse,
} from '../../models/profile.model';
import {
  AnularTimeEntryRequestDto,
  EditTimeEntryRequestDto,
  FichajeTablaResponseDto,
} from '../../models/time-entry.model';
import {
  WorkScheduleRequest,
  WorkScheduleResponse,
  WorkSiteResponseDto,
} from '../../models/work-schedule.model';

@Injectable({
  providedIn: 'root',
})
export class AdminWorkersService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);

  readonly workersSignal = signal<EmployeeResponseDto[]>([]);
  readonly totalWorkersSignal = signal<number>(0);
  readonly jobPositionsSignal = signal<JobPositionUiDto[]>([]);

  obtenerTrabajadores(page: number = 0, size: number = 50): Observable<EmployeeResponseDto[]> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http
      .get<SpringPageResponse<EmployeeResponseDto>>(`${this.BASE_URL}user/employees`, { params })
      .pipe(
        map((response) => response.content ?? []),
        tap((workers) => this.workersSignal.set(workers)),
      );
  }

  obtenerTrabajadoresPaginados(
    page: number = 0,
    size: number = 10,
  ): Observable<SpringPageResponse<EmployeeResponseDto>> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http
      .get<SpringPageResponse<EmployeeResponseDto>>(`${this.BASE_URL}user/employees`, { params })
      .pipe(
        tap((response) => {
          const workers = response.content ?? [];
          this.workersSignal.set(workers);
          this.totalWorkersSignal.set(response.totalElements ?? workers.length);
        }),
      );
  }

  obtenerTrabajadorPorId(id: string): Observable<EmployeeResponseDto> {
    return this.http.get<EmployeeResponseDto>(`${this.BASE_URL}user/employees/${id}`);
  }

  obtenerFichajesPorEmpleado(
    employeeId: string,
    page: number = 0,
    size: number = 10,
  ): Observable<SpringPageResponse<FichajeTablaResponseDto>> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<SpringPageResponse<FichajeTablaResponseDto>>(
      `${this.BASE_URL}time-entries/employee/${employeeId}`,
      { params },
    );
  }

  editarFichaje(id: string, dto: EditTimeEntryRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}time-entries/${id}/edit`, dto);
  }

  anularFichaje(id: string, dto: AnularTimeEntryRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}time-entries/${id}/anular`, dto);
  }

  obtenerPuestosTrabajo(): Observable<JobPositionUiDto[]> {
    return this.http.get<JobPositionResponseDto[]>(`${this.BASE_URL}jobPosition`).pipe(
      map((puestos) => puestos.map((puesto) => ({ id: puesto.id, title: puesto.nombre }))),
      tap((puestos) => this.jobPositionsSignal.set(puestos)),
    );
  }

  crearPuestoTrabajo(nombre: string): Observable<JobPositionUiDto> {
    const url = `${this.BASE_URL}jobPosition`;

    return this.http.post<JobPositionResponseDto>(url, { nombre }).pipe(
      tap({
        error: (err) => console.error('[AdminWorkersService] Error crearPuestoTrabajo', err),
      }),
      map((puesto) => ({ id: puesto.id, title: puesto.nombre })),
    );
  }

  eliminarPuestoTrabajo(id: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}jobPosition/${id}`);
  }

  crearEmpleado(dto: CreateEmployeeRequestDto): Observable<void> {
    return this.http.post<void>(`${this.BASE_URL}auth/register-employee`, dto);
  }

  editarPuestoYHorasEmpleado(id: string, dto: EditEmployeeWorkDataRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}user/employees/${id}/work-data`, dto);
  }

  obtenerCentrosTrabajo(): Observable<WorkSiteResponseDto[]> {
    return this.http.get<WorkSiteResponseDto[]>(`${this.BASE_URL}WorkSites`);
  }

  obtenerHorariosEmpleado(employeeId: string): Observable<WorkScheduleResponse[]> {
    return this.http.get<WorkScheduleResponse[]>(`${this.BASE_URL}horario/employee/${employeeId}`);
  }

  asignarHorario(dto: WorkScheduleRequest): Observable<void> {
    return this.http.post<void>(`${this.BASE_URL}horario/asignar`, dto);
  }
}
