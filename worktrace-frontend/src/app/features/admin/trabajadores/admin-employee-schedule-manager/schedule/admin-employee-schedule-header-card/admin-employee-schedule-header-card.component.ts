import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { EmployeeScheduleResponseDto } from '../../admin-employee-schedule.types';

@Component({
  selector: 'app-admin-employee-schedule-header-card',
  templateUrl: './admin-employee-schedule-header-card.component.html',
  styleUrl: './admin-employee-schedule-header-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminEmployeeScheduleHeaderCardComponent {
  public readonly empleado = input<EmployeeScheduleResponseDto | null>(null);
}
