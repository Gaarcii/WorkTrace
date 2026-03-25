import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-worker-estadisticas-balance',
  imports: [CommonModule, MatIconModule],
  templateUrl: './worker-estadisticas-balance.component.html',
  styleUrls: ['./worker-estadisticas-balance.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasBalanceComponent {
  readonly balanceHorario = input.required<string>();
  readonly balanceClass = input.required<string>();
}
