import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-admin-incidencias-header',
  templateUrl: './admin-incidencias-header.component.html',
  styleUrls: ['./admin-incidencias-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminIncidenciasHeaderComponent {
  readonly modelValue = input.required<number>();

  readonly updateModelValue = output<number>();

  setTab(tab: number): void {
    this.updateModelValue.emit(tab);
  }
}
