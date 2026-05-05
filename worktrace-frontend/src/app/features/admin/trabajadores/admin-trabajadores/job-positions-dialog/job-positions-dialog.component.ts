import { Component, ChangeDetectionStrategy, inject, input, output } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { JobPositionRequestDto } from '../../../../../shared/models/profile.model';

@Component({
  selector: 'app-job-positions-dialog',
  templateUrl: './job-positions-dialog.component.html',
  styleUrl: './job-positions-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
})
export class JobPositionsDialogComponent {
  private readonly fb = inject(FormBuilder);

  public readonly isOpen = input.required<boolean>();
  public readonly loading = input.required<boolean>();
  public readonly jobPositions = input.required<JobPositionRequestDto[]>();

  public readonly updateIsOpen = output<boolean>();
  public readonly create = output<string>();
  public readonly confirmDelete = output<JobPositionRequestDto>();

  public readonly newPositionControl: FormControl<string> = this.fb.control('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/.*\S.*/)],
  });

  public onSubmit(event?: Event): void {
    event?.preventDefault();
    event?.stopPropagation();

    const nombre = this.newPositionControl.value.trim();
    if (!nombre) {
      this.newPositionControl.markAsTouched();
      return;
    }

    this.create.emit(nombre);
    this.newPositionControl.reset('');
  }

  public onClose(): void {
    this.updateIsOpen.emit(false);
    this.newPositionControl.reset('');
  }
}
