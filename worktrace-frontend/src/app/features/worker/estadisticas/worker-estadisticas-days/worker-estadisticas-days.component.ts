import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatRippleModule } from '@angular/material/core';
import { ResumenDia } from '../worker-estadisticas.component';

@Component({
  selector: 'app-worker-estadisticas-days',
  imports: [CommonModule, MatIconModule, MatRippleModule],
  templateUrl: './worker-estadisticas-days.component.html',
  styleUrls: ['./worker-estadisticas-days.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasDaysComponent {
  readonly resumenDias = input<ResumenDia[] | undefined>([]);
  readonly verDetalleDia = output<ResumenDia>();

  onVerDetalle(dia: ResumenDia): void {
    this.verDetalleDia.emit(dia);
  }
}
