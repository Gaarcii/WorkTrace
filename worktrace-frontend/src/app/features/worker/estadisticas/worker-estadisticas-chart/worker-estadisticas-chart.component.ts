import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import {SemanaData, DiaGrafico} from '../worker-estadisticas.component'

@Component({
  selector: 'app-worker-estadisticas-chart',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './worker-estadisticas-chart.component.html',
  styleUrls: ['./worker-estadisticas-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasChartComponent {
  readonly semanaVisible = input<SemanaData | undefined>();
  readonly totalSemanas = input.required<number>();
  readonly currentWeekIndex = input.required<number>();

  readonly prevWeek = output<void>();
  readonly nextWeek = output<void>();
  readonly diaSeleccionado = output<DiaGrafico>();

  onPrevWeek(): void {
    this.prevWeek.emit();
  }

  onNextWeek(): void {
    this.nextWeek.emit();
  }

  onMostrarTooltip(dia: DiaGrafico): void {
    this.diaSeleccionado.emit(dia);
  }
}
