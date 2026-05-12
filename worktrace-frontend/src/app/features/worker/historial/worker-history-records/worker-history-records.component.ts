import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export interface FormattedRegistro {
  timeEntryId?: number | string;
  tipoEvento: string;
  icono: string;
  tipoStr: string;
  fechaStr: string;
  horaStr: string;
}

@Component({
  selector: 'app-worker-history-records',
  imports: [MatIconModule],
  templateUrl: './worker-history-records.component.html',
  styleUrls: ['./worker-history-records.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerHistoryRecordsComponent {
  readonly registros = input.required<FormattedRegistro[]>();
}
