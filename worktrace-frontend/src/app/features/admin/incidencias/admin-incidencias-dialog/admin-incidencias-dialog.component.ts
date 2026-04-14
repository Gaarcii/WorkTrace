import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminGestionEstado, AdminIncidenciaView } from '../admin-incidencias.types';

@Component({
  selector: 'app-admin-incidencias-dialog',
  imports: [FormsModule],
  templateUrl: './admin-incidencias-dialog.component.html',
  styleUrls: ['./admin-incidencias-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminIncidenciasDialogComponent {
  readonly modelValue = input.required<boolean>();
  readonly incidenciaSeleccionada = input<AdminIncidenciaView | null>(null);
  readonly respuestaAdmin = input.required<string>();
  readonly guardando = input.required<boolean>();
  readonly getInitials = input.required<(name?: string) => string>();
  readonly formatFechaCompleta = input.required<(value: string) => string>();

  readonly updateModelValue = output<boolean>();
  readonly updateRespuestaAdmin = output<string>();
  readonly gestionarIncidencia = output<AdminGestionEstado>();

  closeModal(): void {
    this.updateModelValue.emit(false);
  }

  onRespuestaChange(value: string): void {
    this.updateRespuestaAdmin.emit(value);
  }

  onGestionar(estado: AdminGestionEstado): void {
    this.gestionarIncidencia.emit(estado);
  }
}
