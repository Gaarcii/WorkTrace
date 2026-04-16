import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FormBuilder,
  FormControl,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule, DatePipe } from '@angular/common';
import { forkJoin, finalize, take } from 'rxjs';
import { AdminWorkersService } from '../../../../shared/services/admin/admin-workers.service';
import {
  EmployeeResponseDto,
  EditEmployeeWorkDataRequestDto,
  JobPositionUiDto,
} from '../../../../shared/models/profile.model';
import {
  AnularTimeEntryRequestDto,
  EditTimeEntryRequestDto,
  FichajeTablaResponseDto,
} from '../../../../shared/models/time-entry.model';
import { SnackbarState, TableHeader } from '../admin-trabajadores.types';
import { EmployeeProfileCardComponent } from './employee-profile-card/employee-profile-card.component';
import { EmployeeLocationsMapCardComponent } from './employee-locations-map-card/employee-locations-map-card.component';
import { EmployeeFichajesHistoryCardComponent } from './employee-fichajes-history-card/employee-fichajes-history-card.component';
import { EditFichajeDialogComponent } from './edit-fichaje-dialog/edit-fichaje-dialog.component';
import { EditEmployeeProfileDialogComponent } from './edit-employee-profile-dialog/edit-employee-profile-dialog.component';

@Component({
  selector: 'app-admin-employee-detail',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    EmployeeProfileCardComponent,
    EmployeeLocationsMapCardComponent,
    EmployeeFichajesHistoryCardComponent,
    EditFichajeDialogComponent,
    EditEmployeeProfileDialogComponent,
  ],
  templateUrl: './admin-employee-detail.component.html',
  styleUrl: './admin-employee-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminEmployeeDetailComponent implements OnInit {
  private readonly adminWorkersService = inject(AdminWorkersService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  public readonly employeeId = signal<string>('');
  public readonly empleado = signal<EmployeeResponseDto | null>(null);
  public readonly fichajes = signal<FichajeTablaResponseDto[]>([]);
  public readonly puestosTrabajo = signal<JobPositionUiDto[]>([]);
  public readonly loading = signal<boolean>(true);
  public readonly loadingEditarPerfil = signal<boolean>(false);
  public readonly loadingEditarFichaje = signal<boolean>(false);

  public readonly dialogEditarPerfil = signal<boolean>(false);
  public readonly dialogEditarFichaje = signal<boolean>(false);
  public readonly dialogAnular = signal<boolean>(false);
  public readonly fichajeAAnular = signal<FichajeTablaResponseDto | null>(null);
  public readonly loadingAnular = signal<boolean>(false);
  public readonly snackbar = signal<SnackbarState>({ show: false, message: '', color: 'info' });

  public readonly editFichajePayload = signal<FichajeTablaResponseDto | null>(null);

  public readonly fichajeHeaders: TableHeader[] = [
    { key: 'fecha', title: 'Fecha' },
    { key: 'entrada', title: 'Entrada' },
    { key: 'salida', title: 'Salida' },
    { key: 'ubicacionEntrada', title: 'Ubicación Entrada' },
    { key: 'ubicacionSalida', title: 'Ubicación Salida' },
    { key: 'horas', title: 'Horas' },
    { key: 'acciones', title: 'Acciones' },
  ];

  public readonly anularForm = this.fb.nonNullable.group({
    justificacion: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
  });

  public ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.employeeId.set(id);
      this.cargarDatos(id);
    } else {
      this.mostrarSnackbar('ID de empleado no válido', 'error');
      this.volverALista();
    }
  }

  private cargarDatos(id: string): void {
    this.loading.set(true);

    forkJoin({
      empleado: this.adminWorkersService.obtenerTrabajadorPorId(id),
      fichajes: this.adminWorkersService.obtenerFichajesPorEmpleado(id, 0, 50),
      puestosTrabajo: this.adminWorkersService.obtenerPuestosTrabajo(),
    })
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (data) => {
          this.empleado.set(data.empleado);
          this.fichajes.set(data.fichajes.content ?? []);
          this.puestosTrabajo.set(data.puestosTrabajo);
        },
        error: (err: unknown) => {
          console.error('Error al cargar datos:', err);
          this.mostrarSnackbar('Error al cargar los datos del empleado', 'error');
        },
      });
  }

  public volverALista(): void {
    this.router.navigate(['/admin/trabajadores']);
  }

  public irAHorarios(): void {
    const id = this.employeeId();
    if (id) {
      this.router.navigate(['/admin/horarios', id]);
    }
  }

  public abrirModalAnular(fichaje: FichajeTablaResponseDto): void {
    this.fichajeAAnular.set(fichaje);
    this.anularForm.reset();
    this.dialogAnular.set(true);
  }

  public cerrarModalAnular(): void {
    this.dialogAnular.set(false);
    this.fichajeAAnular.set(null);
    this.anularForm.reset();
  }

  public confirmarAnulacion(): void {
    if (this.anularForm.invalid) {
      this.anularForm.markAllAsTouched();
      return;
    }

    const fichaje = this.fichajeAAnular();
    if (!fichaje) return;

    this.loadingAnular.set(true);
    const dto: AnularTimeEntryRequestDto = {
      justificacion: this.anularForm.getRawValue().justificacion.trim(),
    };

    this.adminWorkersService
      .anularFichaje(fichaje.id, dto)
      .pipe(
        take(1),
        finalize(() => this.loadingAnular.set(false)),
      )
      .subscribe({
        next: () => {
          this.mostrarSnackbar('Fichaje anulado correctamente', 'success');
          this.cerrarModalAnular();
          this.cargarDatos(this.employeeId());
        },
        error: (err: unknown) => {
          console.error('Error al anular fichaje:', err);
          this.mostrarSnackbar('Error al anular el fichaje', 'error');
        },
      });
  }

  public abrirModalEditar(fichaje: FichajeTablaResponseDto): void {
    this.editFichajePayload.set(fichaje);
    this.dialogEditarFichaje.set(true);
  }

  public guardarEdicion(dto: EditTimeEntryRequestDto): void {
    const fichaje = this.editFichajePayload();
    if (!fichaje) return;

    this.loadingEditarFichaje.set(true);

    this.adminWorkersService
      .editarFichaje(fichaje.id, dto)
      .pipe(
        take(1),
        finalize(() => this.loadingEditarFichaje.set(false)),
      )
      .subscribe({
        next: () => {
          this.dialogEditarFichaje.set(false);
          this.mostrarSnackbar('Fichaje editado correctamente', 'success');
          this.cargarDatos(this.employeeId());
        },
        error: () => this.mostrarSnackbar('Error al editar fichaje', 'error'),
      });
  }

  public guardarPerfilEmpleado(dto: EditEmployeeWorkDataRequestDto): void {
    this.loadingEditarPerfil.set(true);

    this.adminWorkersService
      .editarPuestoYHorasEmpleado(this.employeeId(), dto)
      .pipe(
        take(1),
        finalize(() => this.loadingEditarPerfil.set(false)),
      )
      .subscribe({
        next: () => {
          this.dialogEditarPerfil.set(false);
          this.mostrarSnackbar('Perfil actualizado correctamente', 'success');
          this.cargarDatos(this.employeeId());
        },
        error: () => this.mostrarSnackbar('Error al actualizar el perfil', 'error'),
      });
  }

  public mostrarSnackbar(message: string, color: SnackbarState['color']): void {
    this.snackbar.set({ show: true, message, color });
    setTimeout(() => {
      this.snackbar.set({ show: false, message: '', color: 'info' });
    }, 3000);
  }

  public cerrarSnackbar(): void {
    this.snackbar.set({ ...this.snackbar(), show: false });
  }

  public getDireccionFichaje = (
    _id: string,
    lat: number,
    lng: number,
    type: 'start' | 'end',
  ): string => {
    const label = type === 'start' ? 'Entrada' : 'Salida';
    return `${label}: ${lat.toFixed(5)}, ${lng.toFixed(5)}`;
  };

  public calcularHoras = (start: string | undefined, end: string | null | undefined): string => {
    if (!start || !end) {
      return '-';
    }

    const startDate = new Date(start);
    const endDate = new Date(end);

    if (Number.isNaN(startDate.getTime()) || Number.isNaN(endDate.getTime())) {
      return '-';
    }

    const totalMinutes = Math.max(0, Math.round((endDate.getTime() - startDate.getTime()) / 60000));
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    return `${hours}h ${String(minutes).padStart(2, '0')}m`;
  };

  public formatTime24h(value?: string | null): string {
    if (!value) {
      return '-';
    }

    const hhmmMatch = value.match(/^(\d{2}):(\d{2})/);
    if (hhmmMatch) {
      return `${hhmmMatch[1]}:${hhmmMatch[2]}`;
    }

    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return '-';
    }

    return parsed.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  }
}
