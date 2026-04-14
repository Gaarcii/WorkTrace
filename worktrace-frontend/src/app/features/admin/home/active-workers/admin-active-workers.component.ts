import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
} from '@angular/core';
import { Router } from '@angular/router';
import { AdminHomeService } from '../../../../shared/services/admin/admin-home.service';
import { take, finalize } from 'rxjs/operators';
import { ActiveWorkerDto } from '../../../../shared/models/time-entry.model';

export interface ActiveWorkerViewData {
  id: string;
  nombre: string;
  iniciales: string;
  departamento: string;
  avatar: string | null;
  entrada: string;
  retraso: boolean;
  fueraDeTurno: boolean;
  minutosRetraso: number;
  estado: string;
}

@Component({
  selector: 'app-admin-active-workers',
  templateUrl: './admin-active-workers.component.html',
  styleUrls: ['./admin-active-workers.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminActiveWorkersComponent implements OnInit {
  private readonly adminHomeService = inject(AdminHomeService);
  private readonly router = inject(Router);

  readonly loading = signal<boolean>(true);

  readonly trabajadoresList = computed<ActiveWorkerViewData[]>(() => {
    const workers = this.adminHomeService.activeWorkersSignal();
    return workers.map((worker) => this.mapToViewData(worker));
  });

  readonly trabajadoresListFiltrados = computed<ActiveWorkerViewData[]>(() => {
    return this.trabajadoresList();
  });

  readonly trabajadoresActivos = computed<number>(() => this.trabajadoresListFiltrados().length);

  ngOnInit(): void {
    this.cargarTrabajadoresActivos();
  }

  private cargarTrabajadoresActivos(): void {
    this.loading.set(true);

    this.adminHomeService
      .obtenerTrabajadoresActivos()
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  private mapToViewData(dto: ActiveWorkerDto): ActiveWorkerViewData {
    const fueraDeTurno = dto.puntualidad == null;
    const punctualityMinutes = Number(dto.puntualidad ?? 0);
    const isLate = punctualityMinutes > 0;
    const delayMinutes = isLate ? punctualityMinutes : 0;

    return {
      id: dto.trabajadorId,
      nombre: dto.nombreCompleto,
      iniciales: this.generarIniciales(dto.nombreCompleto),
      departamento: dto.puestoTrabajo,
      avatar: dto.urlAvatar,
      entrada: this.formatearHoraEntrada(dto.horaFichaje),
      retraso: isLate,
      fueraDeTurno,
      minutosRetraso: delayMinutes,
      estado: fueraDeTurno ? 'Fuera de turno' : isLate ? 'Con retraso' : 'Activo',
    };
  }

  private generarIniciales(nombre: string): string {
    if (!nombre) return '??';
    const partes = nombre.trim().split(' ');
    if (partes.length === 1) return partes[0].substring(0, 2).toUpperCase();
    return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
  }

  formatearRetraso(minutos: number): string {
    if (!minutos || minutos < 60) {
      return `${minutos}m`;
    }
    const horas = Math.floor(minutos / 60);
    const mins = minutos % 60;
    return mins > 0 ? `${horas}h ${mins}m` : `${horas}h`;
  }

  onViewAll(event: Event): void {
    event.preventDefault();
    void this.router.navigate(['/admin/trabajadores']);
  }

  private formatearHoraEntrada(horaFichaje: string): string {
    if (!horaFichaje) {
      return '--:--';
    }

    const isoTimeMatch = horaFichaje.match(/T(\d{2}:\d{2})/);
    if (isoTimeMatch?.[1]) {
      return isoTimeMatch[1];
    }

    const plainTimeMatch = horaFichaje.match(/^(\d{2}:\d{2})(?::\d{2})?$/);
    if (plainTimeMatch?.[1]) {
      return plainTimeMatch[1];
    }

    const fecha = new Date(horaFichaje);
    if (Number.isNaN(fecha.getTime())) {
      return '--:--';
    }

    return fecha.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
