import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { WeekChartDay } from '../admin-home.types';

@Component({
  selector: 'app-admin-weekly-chart',
  templateUrl: './admin-weekly-chart.component.html',
  styleUrls: ['./admin-weekly-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminWeeklyChartComponent {
  readonly rangoSemana = input<string>('');
  readonly fichajesSemana = input<WeekChartDay[]>([]);
  readonly leyendaGrafico = input<string>('');

  readonly prev = output<void>();
  readonly next = output<void>();
  readonly open = output<void>();
  readonly selectDay = output<WeekChartDay>();

  emitPrev(): void {
    this.prev.emit();
  }

  emitNext(): void {
    this.next.emit();
  }

  emitOpen(): void {
    this.open.emit();
  }

  emitSelectDay(dia: WeekChartDay): void {
    this.selectDay.emit(dia);
  }
}
