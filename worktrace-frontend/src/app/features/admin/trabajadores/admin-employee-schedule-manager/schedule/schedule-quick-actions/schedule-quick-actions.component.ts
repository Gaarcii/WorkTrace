import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  DAY_LABELS,
  DayOption,
  EditableDaySchedule,
  ScheduleTemplate,
} from '../../admin-employee-schedule.types';

@Component({
  selector: 'app-schedule-quick-actions',
  imports: [ReactiveFormsModule],
  templateUrl: './schedule-quick-actions.component.html',
  styleUrl: './schedule-quick-actions.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScheduleQuickActionsComponent {
  public readonly schedules = input.required<EditableDaySchedule[]>();

  public readonly activateAll = output<void>();
  public readonly deactivateAll = output<void>();
  public readonly applyToWorkweek = output<number>();
  public readonly applyTemplate = output<ScheduleTemplate>();

  public readonly showWorkweekDialog = signal<boolean>(false);
  public readonly selectedDayControl = new FormControl<number | null>(null, Validators.required);

  public readonly availableDays = computed<DayOption[]>(() =>
    this.schedules()
      .filter((schedule) => this.isComplete(schedule))
      .map((schedule) => ({
        value: schedule.dayOfWeek,
        label: DAY_LABELS[schedule.dayOfWeek] ?? 'Dia',
      })),
  );

  public readonly hasCompleteDay = computed<boolean>(() => this.availableDays().length > 0);

  public onActivateAll(): void {
    this.activateAll.emit();
  }

  public onDeactivateAll(): void {
    this.deactivateAll.emit();
  }

  public onOpenWorkweekDialog(): void {
    if (!this.hasCompleteDay()) {
      return;
    }

    this.selectedDayControl.setValue(this.availableDays()[0]?.value ?? null);
    this.showWorkweekDialog.set(true);
  }

  public onCloseWorkweekDialog(): void {
    this.showWorkweekDialog.set(false);
    this.selectedDayControl.setValue(null);
    this.selectedDayControl.markAsPristine();
    this.selectedDayControl.markAsUntouched();
  }

  public onApplyToWorkweek(): void {
    const selectedDay = this.selectedDayControl.value;
    if (selectedDay === null) {
      this.selectedDayControl.markAsTouched();
      return;
    }

    this.applyToWorkweek.emit(selectedDay);
    this.onCloseWorkweekDialog();
  }

  public onApplyTemplate(template: ScheduleTemplate): void {
    this.applyTemplate.emit(template);
  }

  private isComplete(schedule: EditableDaySchedule): boolean {
    return Boolean(
      schedule.isActive &&
      schedule.siteId &&
      schedule.startTime.trim().length > 0 &&
      schedule.endTime.trim().length > 0,
    );
  }
}
