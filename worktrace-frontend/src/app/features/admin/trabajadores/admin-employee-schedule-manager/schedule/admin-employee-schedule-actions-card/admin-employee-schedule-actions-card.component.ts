import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-admin-employee-schedule-actions-card',
  templateUrl: './admin-employee-schedule-actions-card.component.html',
  styleUrl: './admin-employee-schedule-actions-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminEmployeeScheduleActionsCardComponent {
  public readonly loading = input.required<boolean>();
  public readonly guardando = input.required<boolean>();
  public readonly hayModificaciones = input.required<boolean>();

  public readonly cancel = output<void>();
  public readonly save = output<void>();

  public onCancel(): void {
    this.cancel.emit();
  }

  public onSave(): void {
    this.save.emit();
  }
}
