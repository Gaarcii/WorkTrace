import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { ReactiveFormsModule, FormGroup } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

export interface TipoIncidencia {
  id: string | number;
  name: string;
}

@Component({
  selector: 'app-worker-incidencias-form-modal',
  imports: [ReactiveFormsModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './worker-incidencias-form-modal.component.html',
  styleUrls: ['./worker-incidencias-form-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerIncidenciasFormModalComponent {
  readonly form = input.required<FormGroup>();
  readonly tipos = input.required<TipoIncidencia[]>();
  readonly maxDateStr = input.required<string>();
  readonly mostrarHora = input.required<boolean>();
  readonly loading = input.required<boolean>();
  readonly mensajeError = input.required<string>();

  readonly cerrar = output<void>();
  readonly enviar = output<void>();
  readonly limpiarError = output<void>();

  onCerrar(): void {
    this.cerrar.emit();
  }

  onEnviar(): void {
    this.enviar.emit();
  }

  onLimpiarError(): void {
    this.limpiarError.emit();
  }
}
