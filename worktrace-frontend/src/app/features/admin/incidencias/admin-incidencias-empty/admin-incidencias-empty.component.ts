import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-admin-incidencias-empty',
  templateUrl: './admin-incidencias-empty.component.html',
  styleUrls: ['./admin-incidencias-empty.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminIncidenciasEmptyComponent {
  readonly tab = input.required<number>();
}
