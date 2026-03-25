import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-worker-estadisticas-summary',
  imports: [CommonModule, MatIconModule],
  templateUrl: './worker-estadisticas-summary.component.html',
  styleUrls: ['./worker-estadisticas-summary.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasSummaryComponent {
  readonly horasTrabajadas = input.required<string>();
  readonly balanceHorario = input.required<string>();
  readonly balanceClass = input.required<string>();
  readonly jornadasIncompletas = input.required<number>();
  readonly totalIncidencias = input.required<number>();
}
