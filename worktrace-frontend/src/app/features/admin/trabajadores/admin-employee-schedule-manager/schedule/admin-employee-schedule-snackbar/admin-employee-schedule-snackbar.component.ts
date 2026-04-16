import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { SnackbarState } from '../../../admin-trabajadores.types';

@Component({
  selector: 'app-admin-employee-schedule-snackbar',
  templateUrl: './admin-employee-schedule-snackbar.component.html',
  styleUrl: './admin-employee-schedule-snackbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminEmployeeScheduleSnackbarComponent {
  public readonly snackbar = input.required<SnackbarState>();

  public readonly close = output<void>();

  public onClose(): void {
    this.close.emit();
  }
}
