import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

export interface IncidenciaVista {
  tipoIncidencia?: string;
  estadoTraducido: string;
  estadoColor: string;
  fecha: string;
  horaFormateada: string;
  comentario?: string;
  [key: string]: any;
}

@Component({
  selector: 'app-worker-incidencias-list',
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './worker-incidencias-list.component.html',
  styleUrls: ['./worker-incidencias-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerIncidenciasListComponent {
  readonly incidencias = input.required<IncidenciaVista[]>();
  readonly loading = input.required<boolean>();
}
