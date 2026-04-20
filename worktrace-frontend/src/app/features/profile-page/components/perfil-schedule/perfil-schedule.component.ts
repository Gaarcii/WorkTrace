import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DiaHorario } from '../../../../shared/models/work-schedule.model';

@Component({
  selector: 'app-perfil-schedule',
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './perfil-schedule.component.html',
  styleUrls: ['./perfil-schedule.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilScheduleComponent {
  readonly horario = input.required<DiaHorario[]>();
  readonly loading = input.required<boolean>();
}
