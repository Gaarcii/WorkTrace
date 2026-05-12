import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  CreateEmployeeRequestDto,
  EditEmployeeWorkDataRequestDto,
  EmployeeResponseDto,
  JobPositionResponseDto,
  JobPositionRequestDto,
  SpringPageResponse,
} from '../../models/profile.model';
import {
  VoidTimeEntryRequestDto,
  EditTimeEntryRequestDto,
  TimeEntryTableResponseDto,
} from '../../models/time-entry.model';
import {
  WorkScheduleRequest,
  WorkScheduleResponse,
  WorkSiteResponseDto,
} from '../../models/work-schedule.model';

/**
 * @class AdminWorkersService
 * @description
 * Servicio para la gestión integral de los trabajadores por parte del administrador.
 * Proporciona métodos para el CRUD de empleados, gestión de sus datos laborales
 * (puestos, horarios), y la consulta y modificación de sus fichajes.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminWorkersService {
  private readonly BASE_URL = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);

  readonly workersSignal = signal<EmployeeResponseDto[]>([]);
  readonly totalWorkersSignal = signal<number>(0);
  readonly jobPositionsSignal = signal<JobPositionRequestDto[]>([]);

  /**
   * Obtiene una lista paginada de todos los trabajadores.
   *
   * @description
   * Realiza una petición GET para obtener una página de trabajadores y actualiza
   * los signals `workersSignal` y `totalWorkersSignal` con los datos recibidos.
   *
   * @param page - El número de página a solicitar (basado en 0).
   * @param size - El tamaño de la página.
   * @returns Un `Observable` que emite la respuesta paginada `SpringPageResponse<EmployeeResponseDto>`.
   */
  obtenerTrabajadoresPaginados(
    page = 0,
    size = 10,
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

  /**
   * Obtiene los datos detallados de un trabajador específico por su ID.
   *
   * @param id - El identificador único del trabajador.
   * @returns Un `Observable` que emite un `EmployeeResponseDto` con los datos del trabajador.
   */
  obtenerTrabajadorPorId(id: string): Observable<EmployeeResponseDto> {
    return this.http.get<EmployeeResponseDto>(`${this.BASE_URL}user/employees/${id}`);
  }

  /**
   * Obtiene el historial de fichajes de un empleado de forma paginada.
   *
   * @param employeeId - El ID del empleado cuyos fichajes se quieren obtener.
   * @param page - El número de página a solicitar.
   * @param size - El tamaño de la página.
   * @returns Un `Observable` que emite una respuesta paginada `SpringPageResponse<TimeEntryTableResponseDto>`.
   */
  obtenerFichajesPorEmpleado(
    employeeId: string,
    page = 0,
    size = 10,
  ): Observable<SpringPageResponse<TimeEntryTableResponseDto>> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<SpringPageResponse<TimeEntryTableResponseDto>>(
      `${this.BASE_URL}time-entries/employee/${employeeId}`,
      { params },
    );
  }

  /**
   * Modifica un fichaje existente.
   *
   * @param id - El ID del fichaje a modificar.
   * @param dto - Un objeto `EditTimeEntryRequestDto` con los nuevos datos del fichaje.
   * @returns Un `Observable<void>` que se completa al finalizar la operación.
   */
  updateTimeEntry(id: string, dto: EditTimeEntryRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}time-entries/${id}`, dto);
  }

  /**
   * Anula un fichaje, marcándolo como no válido pero sin eliminarlo.
   *
   * @param id - El ID del fichaje a anular.
   * @param dto - Un objeto `VoidTimeEntryRequestDto` con el motivo de la anulación.
   * @returns Un `Observable<void>` que se completa al finalizar la operación.
   */
  voidTimeEntry(id: string, dto: VoidTimeEntryRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}time-entries/${id}/void`, dto);
  }

  /**
   * Obtiene la lista de todos los puestos de trabajo disponibles.
   *
   * @description
   * Realiza una petición GET y actualiza el `jobPositionsSignal` con los datos transformados.
   *
   * @returns Un `Observable` que emite un array de `JobPositionRequestDto`.
   */
  obtenerPuestosTrabajo(): Observable<JobPositionRequestDto[]> {
    return this.http.get<JobPositionResponseDto[]>(`${this.BASE_URL}job-positions`).pipe(
      map((puestos) => puestos.map((puesto) => ({ id: puesto.id, title: puesto.name }))),
      tap((puestos) => this.jobPositionsSignal.set(puestos)),
    );
  }

  /**
   * Crea un nuevo puesto de trabajo.
   *
   * @param name - El nombre del nuevo puesto de trabajo.
   * @returns Un `Observable` que emite el `JobPositionRequestDto` del puesto recién creado.
   */
  crearPuestoTrabajo(name: string): Observable<JobPositionRequestDto> {
    const url = `${this.BASE_URL}job-positions`;
    return this.http
      .post<JobPositionResponseDto>(url, { name })
      .pipe(map((puesto) => ({ id: puesto.id, title: puesto.name })));
  }

  /**
   * Elimina un puesto de trabajo existente.
   *
   * @param id - El ID del puesto de trabajo a eliminar.
   * @returns Un `Observable<void>` que se completa al finalizar la eliminación.
   */
  eliminarPuestoTrabajo(id: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}job-positions/${id}`);
  }

  /**
   * Registra un nuevo empleado en el sistema.
   *
   * @param dto - Un objeto `CreateEmployeeRequestDto` con los datos del nuevo empleado.
   * @returns Un `Observable<void>` que se completa al finalizar el registro.
   */
  crearEmpleado(dto: CreateEmployeeRequestDto): Observable<void> {
    return this.http
      .post(`${this.BASE_URL}auth/register-employee`, dto, { responseType: 'text' })
      .pipe(map(() => void 0));
  }

  /**
   * Edita los datos laborales de un empleado, como su puesto y horas de contrato.
   *
   * @param id - El ID del empleado a modificar.
   * @param dto - Un objeto `EditEmployeeWorkDataRequestDto` con los nuevos datos.
   * @returns Un `Observable<void>` que se completa al finalizar la actualización.
   */
  editarPuestoYHorasEmpleado(id: string, dto: EditEmployeeWorkDataRequestDto): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}user/employees/${id}/work-data`, dto);
  }

  /**
   * Obtiene la lista de todos los centros de trabajo.
   *
   * @returns Un `Observable` que emite un array de `WorkSiteResponseDto`.
   */
  obtenerCentrosTrabajo(): Observable<WorkSiteResponseDto[]> {
    return this.http.get<WorkSiteResponseDto[]>(`${this.BASE_URL}work-sites`);
  }

  /**
   * Obtiene los horarios de trabajo asignados a un empleado específico.
   *
   * @param employeeId - El ID del empleado.
   * @returns Un `Observable` que emite un array de `WorkScheduleResponse`.
   */
  obtenerHorariosEmpleado(employeeId: string): Observable<WorkScheduleResponse[]> {
    return this.http.get<WorkScheduleResponse[]>(
      `${this.BASE_URL}work-schedules/employee/${employeeId}`,
    );
  }

  /**
   * Asigna un nuevo horario de trabajo a un empleado.
   *
   * @param dto - Un objeto `WorkScheduleRequest` con los detalles de la asignación.
   * @returns Un `Observable<void>` que se completa al finalizar la asignación.
   */
  asignarHorario(dto: WorkScheduleRequest): Observable<void> {
    return this.http.post<void>(`${this.BASE_URL}work-schedules/assign`, dto);
  }
}
