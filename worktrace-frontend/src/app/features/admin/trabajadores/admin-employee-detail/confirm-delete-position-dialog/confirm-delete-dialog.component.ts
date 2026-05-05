import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { JobPositionRequestDto } from '../../../../../shared/models/profile.model';

@Component({
  selector: 'app-confirm-delete-dialog',
  templateUrl: './confirm-delete-dialog.component.html',
  styleUrl: './confirm-delete-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDeleteDialogComponent {
  public readonly isOpen = input.required<boolean>();
  public readonly loading = input.required<boolean>();
  public readonly jobPosition = input<JobPositionRequestDto | null>(null);

  public readonly confirmAction = output<void>();
  public readonly cancelAction = output<void>();

  public onConfirm(): void {
    if (!this.loading()) {
      this.confirmAction.emit();
    }
  }

  public onCancel(): void {
    if (!this.loading()) {
      this.cancelAction.emit();
    }
  }
}
