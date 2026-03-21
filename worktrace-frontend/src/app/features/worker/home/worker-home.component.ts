import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
  DestroyRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { take, finalize } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRippleModule } from '@angular/material/core';
import { WorkerHomeService } from '../../../shared/services/worker/worker-home.service';
import { TimeEntryRequest } from '../../../shared/models/time-entry.model';

@Component({
  selector: 'app-worker-home',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule, MatRippleModule],
  templateUrl: './worker-home.component.html',
  styleUrls: ['./worker-home.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerHomeComponent implements OnInit {
  // Inyecciones
  private readonly workerService = inject(WorkerHomeService);
  private readonly destroyRef = inject(DestroyRef);


  readonly isLoading = signal<boolean>(false);
  readonly errorMsg = signal<string | null>(null);
  readonly currentDate = signal<Date>(new Date());

  readonly resumen = this.workerService.resumenSignal;

  readonly horaActual = computed(() => {
    return this.currentDate().toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
    });
  });

  readonly fechaActual = computed(() => {
    return this.currentDate().toLocaleDateString('es-ES', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  });

  readonly isWorking = computed(() => !!this.resumen()?.horaEntrada);

  readonly estadoTexto = computed(() => {
    if (this.isLoading()) return 'Procesando...';
    return this.isWorking() ? 'Trabajando' : 'Descanso';
  });

  readonly estadoBadgeClass = computed(() => {
    return this.isWorking() ? 'estado-working' : 'estado-inactive';
  });

  readonly estadoDotClass = computed(() => {
    return this.isWorking() ? 'dot-working' : 'dot-inactive';
  });

  readonly botonTexto = computed(() => {
    if (this.isLoading()) return 'PROCESANDO...';
    return this.isWorking() ? 'FICHAR SALIDA' : 'FICHAR ENTRADA';
  });

  readonly botonSubtexto = computed(() => {
    if (this.isLoading()) return 'Espera un momento';
    return this.isWorking()
      ? 'Pulsa para registrar tu salida'
      : 'Pulsa para registrar tu entrada';
  });

  readonly horasTrabajadas = computed(() => {
    const mins = this.resumen()?.minutosAcumulados ?? 0;
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return `${h}h ${m}m`;
  });

  private readonly balanceMinutos = computed(() => {
    const acumulados = this.resumen()?.minutosAcumulados ?? 0;
    const objetivo = this.resumen()?.minutosObjetivo ?? 0;
    return acumulados - objetivo;
  });

  readonly isHorasExtrasPositivo = computed(() => this.balanceMinutos() >= 0);

  readonly horasExtras = computed(() => {
    const balance = Math.abs(this.balanceMinutos());
    const h = Math.floor(balance / 60);
    const m = balance % 60;
    const signo = this.isHorasExtrasPositivo() ? '+' : '-';
    return `${signo} ${h}h ${m}m`;
  });

  readonly registrosFormateados = computed(() => {
    const registros = this.resumen()?.ultimosFichajes ?? [];
    return registros.map((fichaje) => {
      const fechaObj = new Date(fichaje.fecha);
      const isEntrada = fichaje.tipoEvento.toUpperCase() === 'ENTRADA';
      return {
        ...fichaje,
        horaStr: fechaObj.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
        fechaStr: fechaObj.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' }),
        icono: isEntrada ? 'login' : 'logout',
        tipoStr: isEntrada ? 'Entrada' : 'Salida',
      };
    });
  });

  ngOnInit(): void {
    this.workerService.obtenerResumenDiario().pipe(take(1)).subscribe();

    const intervalId = setInterval(() => {
      this.currentDate.set(new Date());
    }, 1000);

    this.destroyRef.onDestroy(() => {
      clearInterval(intervalId);
    });
  }

  clearError(): void {
    this.errorMsg.set(null);
  }

  async handleFichaje(): Promise<void> {
    if (this.isLoading()) return;

    this.isLoading.set(true);
    this.errorMsg.set(null);

    try {
      const coords = await this.getGeolocation();
      const request: TimeEntryRequest = {
        lat: coords.latitude,
        lng: coords.longitude,
        accuracyMeters: coords.accuracy,
      };

      this.workerService
        .fichar(request)
        .pipe(
          take(1),
          finalize(() => this.isLoading.set(false))
        )
        .subscribe({
          error: (err: unknown) => {
            const msj = err instanceof Error ? err.message : 'Error al registrar el fichaje';
            this.errorMsg.set(msj);
          },
        });
    } catch (err: unknown) {
      const msj = err instanceof Error ? err.message : 'No se pudo obtener la ubicación';
      this.errorMsg.set(msj);
      this.isLoading.set(false);
    }
  }

  private getGeolocation(): Promise<GeolocationCoordinates> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocalización no soportada por el navegador'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => resolve(position.coords),
        (error) => {
          let mensaje = 'Error al obtener ubicación';
          if (error.code === error.PERMISSION_DENIED) mensaje = 'Permiso de ubicación denegado';
          else if (error.code === error.POSITION_UNAVAILABLE) mensaje = 'Ubicación no disponible';
          else if (error.code === error.TIMEOUT) mensaje = 'Tiempo de espera agotado';
          reject(new Error(mensaje));
        },
        { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 }
      );
    });
  }
}