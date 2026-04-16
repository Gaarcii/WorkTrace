import { ChangeDetectionStrategy, Component, computed, effect, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  DAY_LABELS,
  EditableDaySchedule,
  WorkSiteResponseDto,
} from '../../admin-employee-schedule.types';

interface ScheduleDayRowForm {
  isActive: FormControl<boolean>;
  siteId: FormControl<string>;
  startTime: FormControl<string>;
  endTime: FormControl<string>;
}

@Component({
  selector: 'app-schedule-day-row',
  imports: [ReactiveFormsModule],
  templateUrl: './schedule-day-row.component.html',
  styleUrl: './schedule-day-row.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScheduleDayRowComponent {
  public readonly schedule = input.required<EditableDaySchedule>();
  public readonly workSites = input.required<WorkSiteResponseDto[]>();

  public readonly updateSchedule = output<EditableDaySchedule>();
  public readonly copyToAll = output<EditableDaySchedule>();

  public readonly scheduleForm = new FormGroup<ScheduleDayRowForm>({
    isActive: new FormControl<boolean>(false, { nonNullable: true }),
    siteId: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    startTime: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    endTime: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  public readonly dayName = computed<string>(() => DAY_LABELS[this.schedule().dayOfWeek] ?? '');

  public readonly isConfigComplete = computed<boolean>(() => {
    const values = this.scheduleForm.getRawValue();
    return Boolean(values.siteId.trim() && values.startTime.trim() && values.endTime.trim());
  });

  private readonly syncInputEffect = effect(() => {
    const currentSchedule = this.schedule();

    this.scheduleForm.setValue(
      {
        isActive: currentSchedule.isActive,
        siteId: currentSchedule.siteId ?? '',
        startTime: currentSchedule.startTime,
        endTime: currentSchedule.endTime,
      },
      { emitEvent: false },
    );

    this.toggleDetailControls(currentSchedule.isActive);
  });

  public onActiveChanged(event: Event): void {
    const checkbox = event.target as HTMLInputElement | null;
    const isActive = Boolean(checkbox?.checked);

    this.scheduleForm.controls.isActive.setValue(isActive, { emitEvent: false });
    this.toggleDetailControls(isActive);
    this.emitCurrentSchedule();
  }

  public onFieldChanged(): void {
    this.emitCurrentSchedule();
  }

  public onCopyToAll(): void {
    const current = this.getCurrentSchedule();
    if (!current.isActive || !this.isConfigComplete()) {
      return;
    }

    this.copyToAll.emit(current);
  }

  private emitCurrentSchedule(): void {
    this.updateSchedule.emit(this.getCurrentSchedule());
  }

  private getCurrentSchedule(): EditableDaySchedule {
    const values = this.scheduleForm.getRawValue();

    return {
      ...this.schedule(),
      isActive: values.isActive,
      siteId: values.siteId.trim().length > 0 ? values.siteId : null,
      startTime: values.startTime,
      endTime: values.endTime,
    };
  }

  private toggleDetailControls(isActive: boolean): void {
    if (isActive) {
      this.scheduleForm.controls.siteId.enable({ emitEvent: false });
      this.scheduleForm.controls.startTime.enable({ emitEvent: false });
      this.scheduleForm.controls.endTime.enable({ emitEvent: false });
      return;
    }

    this.scheduleForm.controls.siteId.disable({ emitEvent: false });
    this.scheduleForm.controls.startTime.disable({ emitEvent: false });
    this.scheduleForm.controls.endTime.disable({ emitEvent: false });
  }
}
