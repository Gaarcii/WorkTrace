import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { JobPositionUiDto } from '../../../../../shared/models/profile.model';

@Component({
  selector: 'app-confirm-delete-position-dialog',
  templateUrl: './confirm-delete-position-dialog.component.html',
  styleUrl: './confirm-delete-position-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDeletePositionDialogComponent {
  @Input({ required: true }) modelValue = false;
  @Input({ required: true }) loading = false;
  @Input() puestoABorrar: JobPositionUiDto | null = null;

  @Output() modelValueChange = new EventEmitter<boolean>();
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}
