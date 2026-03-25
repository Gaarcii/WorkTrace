import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DiaHorario } from '../../../../shared/models/work-schedule.model';

@Component({
  selector: 'app-worker-perfil-schedule',
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './worker-perfil-schedule.component.html',
  styleUrls: ['./worker-perfil-schedule.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerPerfilScheduleComponent {
  readonly horario = input.required<DiaHorario[]>();
  readonly loading = input.required<boolean>();
}