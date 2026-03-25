import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IncidenciaVista } from '../worker-estadisticas.component';

@Component({
  selector: 'app-worker-estadisticas-incidences',
  imports: [CommonModule],
  templateUrl: './worker-estadisticas-incidences.component.html',
  styleUrls: ['./worker-estadisticas-incidences.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasIncidencesComponent {
  readonly incidencias = input.required<IncidenciaVista[]>();
}
