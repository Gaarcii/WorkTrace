import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { take } from 'rxjs/operators';
import { format, formatDistanceToNow } from 'date-fns';
import { es } from 'date-fns/locale';
import { AdminIncidenciasService } from '../../../shared/services/admin/admin-incidencias.service';
import { AdminGestionEstado, AdminIncidenciaView } from './admin-incidencias.types';
import { AdminIncidenciasHeaderComponent } from './admin-incidencias-header/admin-incidencias-header.component';
import { AdminIncidenciasLoadingComponent } from './admin-incidencias-loading/admin-incidencias-loading.component';
import { AdminIncidenciasEmptyComponent } from './admin-incidencias-empty/admin-incidencias-empty.component';
import { AdminIncidenciasListComponent } from './admin-incidencias-list/admin-incidencias-list.component';
import { AdminIncidenciasDialogComponent } from './admin-incidencias-dialog/admin-incidencias-dialog.component';
import { AdminIncidenciasSnackbarComponent } from './admin-incidencias-snackbar/admin-incidencias-snackbar.component';

@Component({
  selector: 'app-incidencias.component',
  imports: [
    AdminIncidenciasHeaderComponent,
    AdminIncidenciasLoadingComponent,
    AdminIncidenciasEmptyComponent,
    AdminIncidenciasListComponent,
    AdminIncidenciasDialogComponent,
    AdminIncidenciasSnackbarComponent,
  ],
  templateUrl: './admin-incidencias.component.html',
  styleUrl: './admin-incidencias.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminIncidenciasComponent implements OnInit {
  private readonly incidenciasService = inject(AdminIncidenciasService);

  readonly tab = signal<number>(0);
  readonly loading = signal<boolean>(true);

  readonly dialogo = signal<boolean>(false);
  readonly incidenciaSeleccionada = signal<AdminIncidenciaView | null>(null);
  readonly respuestaAdmin = signal<string>('');
  readonly guardando = signal<boolean>(false);

  readonly snackbar = signal<boolean>(false);
  readonly snackbarMessage = signal<string>('');

  readonly incidenciasFiltradas = computed<AdminIncidenciaView[]>(() => {
    return this.tab() === 0
      ? this.incidenciasService.incidenciasPendientesSignal()
      : this.incidenciasService.incidenciasHistorialSignal();
  });

  readonly getStatusColor = (status: string): string => {
    const normalized = (status || '').toLowerCase();

    if (normalized === 'resuelta' || normalized === 'resolved') {
      return 'status-resuelta';
    }

    if (normalized === 'rechazada' || normalized === 'rejected') {
      return 'status-rechazada';
    }

    return 'status-pendiente';
  };

  readonly getStatusChipColor = (status: string): string => {
    const normalized = (status || '').toLowerCase();

    if (normalized === 'resuelta' || normalized === 'resolved') {
      return 'chip-resuelta';
    }

    if (normalized === 'rechazada' || normalized === 'rejected') {
      return 'chip-rechazada';
    }

    return 'chip-pendiente';
  };

  readonly getInitials = (fullName?: string): string => {
    if (!fullName) {
      return '??';
    }

    return fullName
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  };

  readonly formatTiempo = (value: string): string => {
    if (!value) {
      return '';
    }

    try {
      return formatDistanceToNow(new Date(value), {
        addSuffix: true,
        locale: es,
      });
    } catch {
      return '';
    }
  };

  readonly formatFechaCompleta = (value: string): string => {
    if (!value) {
      return '';
    }

    try {
      return format(new Date(value), 'dd/MM/yyyy HH:mm', { locale: es });
    } catch {
      return '';
    }
  };

  ngOnInit(): void {
    this.cargarIncidencias();
  }

  setTab(value: number): void {
    this.tab.set(value);
    this.cargarIncidencias();
  }

  abrirDetalle(incidencia: AdminIncidenciaView): void {
    this.incidenciaSeleccionada.set(incidencia);
    this.respuestaAdmin.set(incidencia.admin_response ?? '');
    this.dialogo.set(true);
  }

  cerrarDialogo(): void {
    this.dialogo.set(false);
    this.incidenciaSeleccionada.set(null);
    this.respuestaAdmin.set('');
  }

  gestionarIncidencia(estado: AdminGestionEstado): void {
    const incidencia = this.incidenciaSeleccionada();
    if (!incidencia || this.guardando()) {
      return;
    }

    this.guardando.set(true);

    this.incidenciasService
      .gestionarIncidencia(incidencia.id, {
        estado: estado === 'Resuelta' ? 'RESOLVED' : 'REJECTED',
        respuestaAdmin: this.respuestaAdmin(),
      })
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarDialogo();
          this.snackbarMessage.set('Incidencia gestionada correctamente');
          this.snackbar.set(true);
          this.cargarIncidencias();
          this.incidenciasService.obtenerHistorial().pipe(take(1)).subscribe();
        },
        error: () => {
          this.guardando.set(false);
          this.snackbarMessage.set('No se pudo gestionar la incidencia');
          this.snackbar.set(true);
        },
      });
  }

  private cargarIncidencias(): void {
    this.loading.set(true);

    const request =
      this.tab() === 0
        ? this.incidenciasService.obtenerPendientes()
        : this.incidenciasService.obtenerHistorial();

    request.pipe(take(1)).subscribe({
      next: () => this.loading.set(false),
      error: () => this.loading.set(false),
    });
  }
}
