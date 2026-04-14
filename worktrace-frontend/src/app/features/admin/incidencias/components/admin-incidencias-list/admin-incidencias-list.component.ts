import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { AdminIncidenciaView } from '../../admin-incidencias.types';

@Component({
  selector: 'app-admin-incidencias-list',
  templateUrl: './admin-incidencias-list.component.html',
  styleUrls: ['./admin-incidencias-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminIncidenciasListComponent {
  readonly incidencias = input.required<AdminIncidenciaView[]>();
  readonly getStatusColor = input.required<(status: string) => string>();
  readonly getStatusChipColor = input.required<(status: string) => string>();
  readonly getInitials = input.required<(name?: string) => string>();
  readonly formatTiempo = input.required<(value: string) => string>();

  readonly abrirDetalle = output<AdminIncidenciaView>();

  emitDetalle(incidencia: AdminIncidenciaView): void {
    this.abrirDetalle.emit(incidencia);
  }
}
