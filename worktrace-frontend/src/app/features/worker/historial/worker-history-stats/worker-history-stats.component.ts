import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'app-worker-history-stats',
  templateUrl: './worker-history-stats.component.html',
  styleUrls: ['./worker-history-stats.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerHistoryStatsComponent {
  readonly titulo = input.required<string>();
  readonly horas = input.required<string>();
  readonly balance = input.required<string>();
  readonly isPositive = input.required<boolean>();
  readonly isWeekly = input<boolean>(false);
}
