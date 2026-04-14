import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ActiveWorkerCard } from '../admin-home.types';

@Component({
  selector: 'app-admin-active-workers',
  templateUrl: './admin-active-workers.component.html',
  styleUrls: ['./admin-active-workers.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminActiveWorkersComponent {
  readonly loading = input.required<boolean>();
  readonly trabajadoresList = input<ActiveWorkerCard[]>([]);
  readonly trabajadoresListFiltrados = input<ActiveWorkerCard[]>([]);
  readonly trabajadoresActivos = input.required<number>();

  formatearRetraso(minutos: number): string {
    if (!minutos || minutos < 60) {
      return `${minutos}m`;
    }

    const horas = Math.floor(minutos / 60);
    const mins = minutos % 60;
    return mins > 0 ? `${horas}h ${mins}m` : `${horas}h`;
  }
}
