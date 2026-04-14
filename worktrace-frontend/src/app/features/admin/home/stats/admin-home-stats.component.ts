import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-admin-home-stats',
  templateUrl: './admin-home-stats.component.html',
  styleUrls: ['./admin-home-stats.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminHomeStatsComponent {
  readonly loading = input.required<boolean>();
  readonly trabajadoresActivos = input.required<number>();
  readonly totalTrabajadores = input.required<number>();
  readonly fichajesHoy = input.required<number>();
  readonly alertasPendientes = input.required<number>();
  readonly horasTotales = input.required<number>();
}
