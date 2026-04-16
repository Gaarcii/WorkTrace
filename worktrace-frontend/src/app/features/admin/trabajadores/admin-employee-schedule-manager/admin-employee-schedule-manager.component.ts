import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, finalize, take } from 'rxjs';
import { AdminWorkersService } from '../../../../shared/services/admin/admin-workers.service';
import {
  DayOfWeek,
  WorkScheduleRequest,
  WorkScheduleResponse,
} from '../../../../shared/models/work-schedule.model';
import { SnackbarState } from '../admin-trabajadores.types';
import {
  DAY_KEYS,
  EditableDaySchedule,
  EmployeeScheduleResponseDto,
  ScheduleTemplate,
  TemplateDefinition,
  WorkSiteResponseDto,
} from './admin-employee-schedule.types';
import { AdminEmployeeScheduleHeaderCardComponent } from './schedule/admin-employee-schedule-header-card/admin-employee-schedule-header-card.component';
import { AdminEmployeeScheduleContentComponent } from './schedule/admin-employee-schedule-content/admin-employee-schedule-content.component';
import { AdminEmployeeScheduleActionsCardComponent } from './schedule/admin-employee-schedule-actions-card/admin-employee-schedule-actions-card.component';
import { AdminEmployeeScheduleSnackbarComponent } from './schedule/admin-employee-schedule-snackbar/admin-employee-schedule-snackbar.component';

@Component({
  selector: 'app-admin-employee-schedule-manager',
  imports: [
    AdminEmployeeScheduleHeaderCardComponent,
    AdminEmployeeScheduleContentComponent,
    AdminEmployeeScheduleActionsCardComponent,
    AdminEmployeeScheduleSnackbarComponent,
  ],
  templateUrl: './admin-employee-schedule-manager.component.html',
  styleUrl: './admin-employee-schedule-manager.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminEmployeeScheduleManagerComponent implements OnInit {
  private readonly adminWorkersService = inject(AdminWorkersService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private snackbarTimer: ReturnType<typeof setTimeout> | null = null;

  public readonly employeeId = signal<string>('');
  public readonly empleado = signal<EmployeeScheduleResponseDto | null>(null);
  public readonly schedules = signal<EditableDaySchedule[]>(this.createEmptySchedules());
  public readonly initialSchedules = signal<EditableDaySchedule[]>(this.createEmptySchedules());
  public readonly workSites = signal<WorkSiteResponseDto[]>([]);
  public readonly loading = signal<boolean>(true);
  public readonly guardando = signal<boolean>(false);
  public readonly error = signal<string | null>(null);

  public readonly snackbar = signal<SnackbarState>({
    show: false,
    message: '',
    color: 'info',
  });

  public readonly hayModificaciones = computed<boolean>(
    () => !this.areSchedulesEqual(this.schedules(), this.initialSchedules()),
  );

  public ngOnInit(): void {
    const idTrabajador = this.route.snapshot.paramMap.get('idTrabajador');
    if (!idTrabajador) {
      this.showNotification('ID de empleado invalido', 'error');
      void this.router.navigate(['/admin/trabajadores']);
      return;
    }

    this.employeeId.set(idTrabajador);
    this.cargarDatosIniciales(idTrabajador);
  }

  public volverADetalle(): void {
    const id = this.employeeId();
    if (id) {
      void this.router.navigate(['/admin/trabajadores', id]);
      return;
    }

    void this.router.navigate(['/admin/trabajadores']);
  }

  public actualizarHorario(updatedSchedule: EditableDaySchedule): void {
    this.schedules.update((currentSchedules) =>
      currentSchedules.map((schedule) =>
        schedule.dayOfWeek === updatedSchedule.dayOfWeek ? { ...updatedSchedule } : schedule,
      ),
    );
  }

  public copiarATodos(baseSchedule: EditableDaySchedule): void {
    if (!this.isScheduleComplete(baseSchedule)) {
      this.showNotification('Debes completar el dia antes de copiar', 'warning');
      return;
    }

    this.schedules.update((currentSchedules) =>
      currentSchedules.map((schedule) => ({
        ...schedule,
        isActive: true,
        siteId: baseSchedule.siteId,
        startTime: baseSchedule.startTime,
        endTime: baseSchedule.endTime,
      })),
    );

    this.showNotification('Configuracion copiada a todos los dias', 'success');
  }

  public activarTodos(): void {
    this.schedules.update((currentSchedules) =>
      currentSchedules.map((schedule) => ({ ...schedule, isActive: true })),
    );
  }

  public desactivarTodos(): void {
    this.schedules.update((currentSchedules) =>
      currentSchedules.map((schedule) => ({ ...schedule, isActive: false })),
    );
  }

  public aplicarASemanaLaboral(baseDay: number): void {
    const sourceSchedule = this.schedules().find((schedule) => schedule.dayOfWeek === baseDay);
    if (!sourceSchedule || !this.isScheduleComplete(sourceSchedule)) {
      this.showNotification('Selecciona un dia base con configuracion completa', 'warning');
      return;
    }

    this.schedules.update((currentSchedules) =>
      currentSchedules.map((schedule) => {
        if (schedule.dayOfWeek > 4) {
          return schedule;
        }

        return {
          ...schedule,
          isActive: true,
          siteId: sourceSchedule.siteId,
          startTime: sourceSchedule.startTime,
          endTime: sourceSchedule.endTime,
        };
      }),
    );

    this.showNotification('Configuracion aplicada de lunes a viernes', 'success');
  }

  public aplicarPlantilla(template: ScheduleTemplate): void {
    const templates: Record<ScheduleTemplate, TemplateDefinition> = {
      morning: { startTime: '08:00', endTime: '15:00' },
      full: { startTime: '09:00', endTime: '18:00' },
    };

    const selectedTemplate = templates[template];
    const fallbackSiteId =
      this.schedules().find((schedule) => this.isScheduleComplete(schedule))?.siteId ?? null;

    this.schedules.update((currentSchedules) =>
      currentSchedules.map((schedule) => {
        if (schedule.dayOfWeek > 4) {
          return schedule;
        }

        return {
          ...schedule,
          isActive: true,
          siteId: schedule.siteId ?? fallbackSiteId,
          startTime: selectedTemplate.startTime,
          endTime: selectedTemplate.endTime,
        };
      }),
    );

    this.showNotification('Plantilla aplicada a la semana laboral', 'success');
  }

  public guardarHorarios(): void {
    const activeSchedules = this.schedules().filter((schedule) => schedule.isActive);

    if (activeSchedules.length === 0) {
      this.showNotification('Activa al menos un dia para guardar', 'warning');
      return;
    }

    if (activeSchedules.some((schedule) => !this.isScheduleComplete(schedule))) {
      this.showNotification('Hay dias activos con campos incompletos', 'warning');
      return;
    }

    const payload: WorkScheduleRequest = {
      employeeId: this.employeeId(),
      schedules: activeSchedules.map((schedule) => ({
        dayOfWeek: schedule.dayKey,
        startTime: schedule.startTime,
        endTime: schedule.endTime,
        siteId: schedule.siteId ?? '',
      })),
    };

    this.guardando.set(true);

    this.adminWorkersService
      .asignarHorario(payload)
      .pipe(
        take(1),
        finalize(() => this.guardando.set(false)),
      )
      .subscribe({
        next: () => {
          this.initialSchedules.set(this.cloneSchedules(this.schedules()));
          this.showNotification('Horarios guardados correctamente', 'success');
        },
        error: (err: unknown) => {
          console.error('Error al guardar horarios:', err);
          this.showNotification('No se pudieron guardar los horarios', 'error');
        },
      });
  }

  public closeSnackbar(): void {
    this.snackbar.set({ ...this.snackbar(), show: false });
  }

  private cargarDatosIniciales(id: string): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      empleado: this.adminWorkersService.obtenerTrabajadorPorId(id),
      horarios: this.adminWorkersService.obtenerHorariosEmpleado(id),
      workSites: this.adminWorkersService.obtenerCentrosTrabajo(),
    })
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: ({ empleado, horarios, workSites }) => {
          const employeeData = empleado as EmployeeScheduleResponseDto;
          const mappedSchedules = this.buildSchedulesFromBackend(horarios, workSites);

          this.empleado.set(employeeData);
          this.workSites.set(workSites);
          this.schedules.set(mappedSchedules);
          this.initialSchedules.set(this.cloneSchedules(mappedSchedules));
        },
        error: (err: unknown) => {
          console.error('Error al cargar horarios:', err);
          this.error.set('No se pudieron cargar los datos del empleado');
          this.showNotification('Error al cargar la informacion de horarios', 'error');
        },
      });
  }

  private buildSchedulesFromBackend(
    backendSchedules: readonly WorkScheduleResponse[],
    workSites: readonly WorkSiteResponseDto[],
  ): EditableDaySchedule[] {
    const initial = this.createEmptySchedules();

    if (backendSchedules.length === 0) {
      return initial;
    }

    const siteLookup = new Map<string, string>(
      workSites.map((site) => [this.getSiteLookupKey(site.name, site.address), site.id]),
    );

    for (const backendSchedule of backendSchedules) {
      const dayKey = String(backendSchedule.diaSemana).toUpperCase() as DayOfWeek;
      const dayIndex = DAY_KEYS.indexOf(dayKey);

      if (dayIndex < 0) {
        continue;
      }

      const siteId = siteLookup.get(
        this.getSiteLookupKey(backendSchedule.lugar, backendSchedule.ubicacion),
      );

      initial[dayIndex] = {
        ...initial[dayIndex],
        isActive: true,
        siteId: siteId ?? null,
        startTime: this.normalizeTime(backendSchedule.start),
        endTime: this.normalizeTime(backendSchedule.end),
      };
    }

    return initial;
  }

  private createEmptySchedules(): EditableDaySchedule[] {
    return DAY_KEYS.map((dayKey, index) => ({
      dayOfWeek: index,
      dayKey,
      isActive: false,
      siteId: null,
      startTime: '',
      endTime: '',
    }));
  }

  private normalizeTime(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    const timeMatch = value.match(/^(\d{2}):(\d{2})/);
    if (!timeMatch) {
      return '';
    }

    return `${timeMatch[1]}:${timeMatch[2]}`;
  }

  private getSiteLookupKey(
    name: string | null | undefined,
    address: string | null | undefined,
  ): string {
    const normalizedName = (name ?? '').trim().toLowerCase();
    const normalizedAddress = (address ?? '').trim().toLowerCase();
    return `${normalizedName}::${normalizedAddress}`;
  }

  private isScheduleComplete(schedule: EditableDaySchedule): boolean {
    return Boolean(schedule.siteId && schedule.startTime && schedule.endTime);
  }

  private cloneSchedules(schedules: readonly EditableDaySchedule[]): EditableDaySchedule[] {
    return schedules.map((schedule) => ({ ...schedule }));
  }

  private areSchedulesEqual(
    first: readonly EditableDaySchedule[],
    second: readonly EditableDaySchedule[],
  ): boolean {
    if (first.length !== second.length) {
      return false;
    }

    return first.every((schedule, index) => {
      const compared = second[index];
      return (
        schedule.dayOfWeek === compared.dayOfWeek &&
        schedule.dayKey === compared.dayKey &&
        schedule.isActive === compared.isActive &&
        schedule.siteId === compared.siteId &&
        schedule.startTime === compared.startTime &&
        schedule.endTime === compared.endTime
      );
    });
  }

  private showNotification(message: string, color: SnackbarState['color']): void {
    if (this.snackbarTimer) {
      clearTimeout(this.snackbarTimer);
    }

    this.snackbar.set({ show: true, message, color });

    this.snackbarTimer = setTimeout(() => {
      this.snackbar.set({ show: false, message: '', color: 'info' });
      this.snackbarTimer = null;
    }, 3000);
  }
}
