import { Component, ChangeDetectionStrategy, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-worker-incidencias-header',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './worker-incidencias-header.component.html',
  styleUrls: ['./worker-incidencias-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerIncidenciasHeaderComponent {
  readonly abrirFormulario = output<void>();

  onAbrir(): void {
    this.abrirFormulario.emit();
  }
}
