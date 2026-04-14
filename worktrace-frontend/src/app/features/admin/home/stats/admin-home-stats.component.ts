import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
} from '@angular/core';
import { AdminHomeService } from '../../../../shared/services/admin/admin-home.service';
import { forkJoin } from 'rxjs';
import { take, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-admin-home-stats',
  templateUrl: './admin-home-stats.component.html',
  styleUrls: ['./admin-home-stats.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminHomeStatsComponent implements OnInit {
  // Inyección estricta de dependencias
  private readonly adminHomeService = inject(AdminHomeService);

  readonly loading = signal<boolean>(true);

  readonly trabajadoresActivos = computed<number>(
    () => this.adminHomeService.activeWorkersSignal().length,
  );

  readonly totalTrabajadores = computed<number>(
    () => this.adminHomeService.totalWorkersSignal() ?? 0,
  );

  readonly fichajesHoy = computed<number>(() => this.adminHomeService.numFichajesHoySignal() ?? 0);

  readonly alertasPendientes = computed<number>(
    () => this.adminHomeService.adminIncidenciasSignal().length,
  );

  readonly horasTotales = computed<number>(() => {
    const horasHoy = this.adminHomeService.horasHoySignal();
    return horasHoy ? Math.floor(horasHoy.minutosTotales / 60) : 0;
  });

  ngOnInit(): void {
    this.cargarDatosDashboard();
  }

  private cargarDatosDashboard(): void {
    this.loading.set(true);

    forkJoin([
      this.adminHomeService.obtenerTrabajadoresActivos(),
      this.adminHomeService.obtenerTotalTrabajadores(),
      this.adminHomeService.obtenerIncidenciasAdmin('PENDING', 0, 10),
      this.adminHomeService.obtenerNumFichajesHoy(),
      this.adminHomeService.obtenerHorasHoy(),
    ])
      .pipe(
        take(1),
        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }
}
