import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DashboardAlert } from '../admin-home.types';

@Component({
  selector: 'app-admin-recent-alerts',
  templateUrl: './admin-recent-alerts.component.html',
  styleUrls: ['./admin-recent-alerts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminRecentAlertsComponent {
  readonly loading = input.required<boolean>();
  readonly alertasRecientes = input<DashboardAlert[]>([]);
  readonly formatTiempo = input.required<(fecha: string) => string>();

  readonly viewAll = output<void>();

  emitViewAll(event: Event): void {
    event.preventDefault();
    this.viewAll.emit();
  }
}
