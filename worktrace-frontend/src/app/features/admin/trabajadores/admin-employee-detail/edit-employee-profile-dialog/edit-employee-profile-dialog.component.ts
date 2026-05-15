import { Component, ChangeDetectionStrategy, inject, input, output, effect } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  EmployeeResponseDto,
  EditEmployeeWorkDataRequestDto,
  JobPositionRequestDto,
} from '../../../../../shared/models/profile.model';

interface EditEmployeeProfileFormGroup {
  positionId: FormControl<string | null>;
  weeklyHours: FormControl<number | null>;
}

@Component({
  selector: 'app-edit-employee-profile-dialog',
  templateUrl: './edit-employee-profile-dialog.component.html',
  styleUrl: './edit-employee-profile-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
})
export class EditEmployeeProfileDialogComponent {
  private readonly fb = inject(FormBuilder);

  public readonly isOpen = input.required<boolean>();
  public readonly empleado = input<EmployeeResponseDto | null>(null);
  public readonly jobPositions = input.required<JobPositionRequestDto[]>();
  public readonly loading = input.required<boolean>();
  public readonly error = input<string | null>(null);

  public readonly closeDialog = output<void>();
  public readonly save = output<EditEmployeeWorkDataRequestDto>();

  public readonly form = this.fb.group<EditEmployeeProfileFormGroup>({
    positionId: this.fb.control<string | null>(null),
    weeklyHours: this.fb.control<number | null>(40, {
      validators: [Validators.required, Validators.min(0), Validators.max(168)],
    }),
  });

  constructor() {
    effect(() => {
      const data = this.empleado();
      const jobPositions = this.jobPositions();
      const open = this.isOpen();

      if (open && data) {
        this.form.patchValue({
          positionId: this.resolvePositionId(data, jobPositions),
          weeklyHours: this.parseWeeklyHours(data.weeklyHours),
        });
      } else if (!open) {
        this.form.reset({ positionId: null, weeklyHours: 40 });
      }
    });
  }

  public onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();
    this.save.emit({
      positionId: rawValue.positionId ?? '',
      weeklyHours: rawValue.weeklyHours ?? 0,
    });
  }

  public onClose(): void {
    if (!this.loading()) {
      this.closeDialog.emit();
    }
  }

  private parseWeeklyHours(value: string): number | null {
    const parsed = Number(value);
    return Number.isNaN(parsed) ? null : parsed;
  }

  private resolvePositionId(
    data: EmployeeResponseDto,
    jobPositions: JobPositionRequestDto[],
  ): string | null {
    if (data.positionId) {
      return data.positionId;
    }

    const normalizedJobPosition = data.jobPosition.trim().toLocaleLowerCase();
    const matchedPosition = jobPositions.find(
      (puesto) => puesto.title.trim().toLocaleLowerCase() === normalizedJobPosition,
    );

    return matchedPosition?.id ?? null;
  }
}
