import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export interface FormattedRecord {
  horaStr: string;
  fechaStr: string;
  icono: string;
  tipoStr: string;
}

@Component({
  selector: 'app-worker-recent-records',
  imports: [MatIconModule],
  templateUrl: './worker-recent-records.component.html',
  styleUrls: ['./worker-recent-records.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerRecentRecordsComponent {
  readonly registros = input.required<FormattedRecord[]>();
}
