import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'app-worker-summary-stats',
  templateUrl: './worker-summary-stats.component.html',
  styleUrls: ['./worker-summary-stats.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerSummaryStatsComponent {
  readonly horasTrabajadas = input.required<string>();
  readonly horasExtras = input.required<string>();
  readonly isHorasExtrasPositivo = input.required<boolean>();
}
