import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatRippleModule } from '@angular/material/core';

export interface DiaSemana {
  fecha: string;
  nombre: string;
  numero: number;
  seleccionado: boolean;
}

@Component({
  selector: 'app-worker-history-days',
  imports: [MatRippleModule],
  templateUrl: './worker-history-days.component.html',
  styleUrls: ['./worker-history-days.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerHistoryDaysComponent {
  readonly dias = input.required<DiaSemana[]>();
  readonly diaSeleccionado = output<DiaSemana>();

  onSelectDia(dia: DiaSemana): void {
    this.diaSeleccionado.emit(dia);
  }
}
