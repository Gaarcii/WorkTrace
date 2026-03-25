import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';

@Component({
  selector: 'app-worker-estadisticas-header',
  imports: [MatIconModule, MatButtonModule, MatButtonToggleModule],
  templateUrl: './worker-estadisticas-header.component.html',
  styleUrls: ['./worker-estadisticas-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerEstadisticasHeaderComponent {
  readonly periodoSeleccionado = input.required<'semana' | 'mes' | 'todo' | 'custom'>();
  readonly mostrarFiltroFechas = input.required<boolean>();

  readonly toggleFiltro = output<void>();
  readonly cambiarPeriodo = output<'semana' | 'mes' | 'todo'>();

  onToggleFiltro(): void {
    this.toggleFiltro.emit();
  }

  onCambiarPeriodo(periodo: string): void {
    this.cambiarPeriodo.emit(periodo as 'semana' | 'mes' | 'todo');
  }
}
