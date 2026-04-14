import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { SelectedDayEntry } from '../admin-home.types';

@Component({
  selector: 'app-admin-selected-day-entries',
  templateUrl: './admin-selected-day-entries.component.html',
  styleUrls: ['./admin-selected-day-entries.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSelectedDayEntriesComponent {
  readonly fichajesDiaSeleccionado = input<SelectedDayEntry[]>([]);
  readonly diaSeleccionado = input<string | null>(null);
  readonly formatFechaDia = input.required<(fechaString: string | null) => string>();
}
