import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { take } from 'rxjs/operators';
import { AdminWorkersService } from '../../../../shared/services/admin/admin-workers.service';
import {
  CreateEmployeeRequestDto,
  EmployeeResponseDto,
  JobPositionUiDto,
} from '../../../../shared/models/profile.model';
import {
  CreateEmployeeForm,
  EmployeeListItem,
  EmployeeTableHeader,
  SnackbarState,
} from '../admin-trabajadores.types';
import { EmployeesTableComponent } from './employee-table/employee-table.component';
import { JobPositionsDialogComponent } from './job-positions-dialog/job-positions-dialog.component';
import { CreateEmployeeDialogComponent } from './admin-crearTrabajador/create-employee-dialog.component';
import { EmployeeHeaderComponent } from './employee-header/employee-header.component';
import { ConfirmDeleteDialogComponent } from '../admin-employee-detail/confirm-delete-position-dialog/confirm-delete-dialog.component';

@Component({
  selector: 'app-trabajadores.component',
  imports: [
    CommonModule,
    FormsModule,
    EmployeeHeaderComponent,
    EmployeesTableComponent,
    JobPositionsDialogComponent,
    ConfirmDeleteDialogComponent,
    CreateEmployeeDialogComponent,
  ],
  templateUrl: './admin-trabajadores.component.html',
  styleUrl: './admin-trabajadores.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminTrabajadoresComponent implements OnInit {
  private readonly workersService = inject(AdminWorkersService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal<boolean>(false);
  readonly empleados = signal<EmployeeListItem[]>([]);
  readonly dialogCrear = signal<boolean>(false);
  readonly dialogPuestos = signal<boolean>(false);
  readonly dialogConfirmarBorrar = signal<boolean>(false);
  readonly snackbar = signal<SnackbarState>({
    show: false,
    message: '',
    color: 'success',
  });

  readonly puestosTrabajo = signal<JobPositionUiDto[]>([]);
  readonly nuevoPuesto = signal<string>('');
  readonly puestoABorrar = signal<JobPositionUiDto | null>(null);

  readonly headers: EmployeeTableHeader[] = [
    { key: 'fullName', title: 'Nombre' },
    { key: 'jobTitle', title: 'Puesto' },
    { key: 'weeklyHours', title: 'Horas' },
    { key: 'isFirstLogin', title: 'Estado' },
    { key: 'createdAt', title: 'Fecha Alta' },
  ];

  readonly formData: CreateEmployeeForm = {
    fullName: '',
    employeeCode: '',
    email: '',
    phone: '',
    weeklyHours: null,
    positionId: null,
  };

  ngOnInit(): void {
    void Promise.all([this.loadEmployees(), this.loadPuestos()]).then(() => {
      if (this.route.snapshot.queryParamMap.get('open') === 'true') {
        this.dialogCrear.set(true);
      }
    });
  }

  formatDate = (dateString: string): string => {
    if (!dateString) {
      return '-';
    }

    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  abrirModalCrear(): void {
    this.dialogCrear.set(true);
  }

  abrirModalPuestosDesdeCrear(): void {
    this.dialogPuestos.set(true);
  }

  cerrarModalCrear(): void {
    this.dialogCrear.set(false);
    this.formData.fullName = '';
    this.formData.employeeCode = '';
    this.formData.email = '';
    this.formData.phone = '';
    this.formData.weeklyHours = null;
    this.formData.positionId = null;
  }

  cerrarModalCrearSeguro(): void {
    if (this.dialogPuestos()) {
      return;
    }
    this.cerrarModalCrear();
  }

  verDetalleEmpleado(item: EmployeeListItem): void {
    void this.router.navigate(['/admin/trabajadores', item.id]);
  }

  crearEmpleado(): void {
    const form = this.formData;
    if (
      !form.fullName ||
      !form.employeeCode ||
      !form.email ||
      !form.phone ||
      !form.weeklyHours ||
      !form.positionId
    ) {
      this.mostrarSnackbar('Completa todos los campos obligatorios', 'warning');
      return;
    }

    const payload: CreateEmployeeRequestDto = {
      email: form.email,
      profile: {
        fullName: form.fullName,
        employeeCode: form.employeeCode,
        phone: form.phone,
        weeklyHours: form.weeklyHours,
        positionId: form.positionId,
      },
    };

    this.loading.set(true);
    this.workersService
      .crearEmpleado(payload)
      .pipe(take(1))
      .subscribe({
        next: async () => {
          await this.onCrearEmpleadoExito();
        },
        error: async (err: unknown) => {
          // Some backends return 2xx with non-JSON bodies, which Angular can surface as HttpErrorResponse.
          if (err instanceof HttpErrorResponse && err.status >= 200 && err.status < 300) {
            await this.onCrearEmpleadoExito();
            return;
          }

          this.loading.set(false);
          this.mostrarSnackbar('No se pudo crear el empleado', 'error');
        },
      });
  }

  private async onCrearEmpleadoExito(): Promise<void> {
    this.loading.set(false);
    this.cerrarModalCrear();
    this.mostrarSnackbar('Empleado creado correctamente', 'success');
    await this.loadEmployees();
  }

  crearPuesto(nombrePuesto?: string): void {
    const nombre = (nombrePuesto ?? this.nuevoPuesto()).trim();
    if (!nombre) {
      this.mostrarSnackbar('Introduce un nombre de puesto válido', 'warning');
      return;
    }

    this.loading.set(true);
    this.workersService
      .crearPuestoTrabajo(nombre)
      .pipe(take(1))
      .subscribe({
        next: async () => {
          this.loading.set(false);
          this.dialogPuestos.set(false);
          this.nuevoPuesto.set('');
          this.mostrarSnackbar('Puesto creado correctamente', 'success');
          await this.loadPuestos();
        },
        error: (err) => {
          const backendMessage =
            (err as { error?: { message?: string } })?.error?.message ??
            (err as { message?: string })?.message ??
            'No se pudo crear el puesto';

          console.error('[AdminTrabajadores] Error creando puesto:', err);
          this.loading.set(false);
          this.mostrarSnackbar(backendMessage, 'error');
        },
      });
  }

  onCreatePuestoEvent(nombre: string): void {
    this.crearPuesto(nombre);
  }

  confirmarBorrarPuesto(puesto: JobPositionUiDto): void {
    this.puestoABorrar.set(puesto);
    this.dialogConfirmarBorrar.set(true);
  }

  borrarPuesto(): void {
    const puesto = this.puestoABorrar();
    if (!puesto) {
      return;
    }

    this.loading.set(true);
    this.workersService
      .eliminarPuestoTrabajo(puesto.id)
      .pipe(take(1))
      .subscribe({
        next: async () => {
          this.loading.set(false);
          this.dialogConfirmarBorrar.set(false);
          this.puestoABorrar.set(null);
          this.mostrarSnackbar('Puesto eliminado correctamente', 'success');
          await this.loadPuestos();
        },
        error: () => {
          this.loading.set(false);
          this.mostrarSnackbar('No se pudo eliminar el puesto', 'error');
        },
      });
  }

  private async loadEmployees(): Promise<void> {
    this.loading.set(true);

    await new Promise<void>((resolve) => {
      this.workersService
        .obtenerTrabajadoresPaginados(0, 200)
        .pipe(take(1))
        .subscribe({
          next: (response) => {
            this.empleados.set(
              (response.content ?? []).map((emp) => {
                const parsedHours = Number(emp.horasSemanales);
                const positionId =
                  (emp as EmployeeResponseDto & { positionId?: string | null }).positionId ?? null;

                return {
                  id: emp.id,
                  fullName: emp.nombre,
                  jobTitle: emp.puesto,
                  weeklyHours: Number.isNaN(parsedHours) ? null : parsedHours,
                  isFirstLogin: emp.estado?.toLowerCase() === 'pendiente',
                  isActive: emp.estado?.toLowerCase() !== 'inactivo',
                  createdAt: emp.fechaAlta,
                  email: emp.email,
                  phone: emp.telefono,
                  avatarUrl: emp.avatarUrl,
                  dni: emp.dni,
                  positionId,
                };
              }),
            );
            this.loading.set(false);
            resolve();
          },
          error: () => {
            this.loading.set(false);
            this.mostrarSnackbar('No se pudieron cargar los empleados', 'error');
            resolve();
          },
        });
    });
  }

  private async loadPuestos(): Promise<void> {
    await new Promise<void>((resolve) => {
      this.workersService
        .obtenerPuestosTrabajo()
        .pipe(take(1))
        .subscribe({
          next: (puestos) => {
            this.puestosTrabajo.set(puestos);
            resolve();
          },
          error: () => {
            this.mostrarSnackbar('No se pudieron cargar los puestos', 'error');
            resolve();
          },
        });
    });
  }

  private mostrarSnackbar(message: string, color: SnackbarState['color']): void {
    this.snackbar.set({ show: true, message, color });
    setTimeout(() => {
      this.snackbar.set({ ...this.snackbar(), show: false });
    }, 3000);
  }
}
