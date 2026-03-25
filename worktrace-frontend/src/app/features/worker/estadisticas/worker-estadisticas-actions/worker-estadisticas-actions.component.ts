import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-worker-estadisticas-actions',
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './worker-estadisticas-actions.component.html',
  styleUrls: ['./worker-estadisticas-actions.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasActionsComponent {
  readonly isDownloadingPdf = input.required<boolean>();

  readonly descargarRegistros = output<void>();
  readonly verHistorialDetallado = output<void>();

  onDescargar(): void {
    this.descargarRegistros.emit();
  }

  onVerHistorial(): void {
    this.verHistorialDetallado.emit();
  }
}
