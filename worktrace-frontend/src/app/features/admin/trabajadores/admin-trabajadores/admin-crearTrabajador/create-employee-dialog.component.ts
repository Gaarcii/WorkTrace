import { ChangeDetectionStrategy, Component, effect, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateEmployeeForm } from '../../admin-trabajadores.types';
import { JobPositionRequestDto } from '../../../../../shared/models/profile.model';
import { DayOfWeek, WorkSiteResponseDto } from '../../../../../shared/models/work-schedule.model';
import {
  DAY_KEYS,
  DAY_LABELS,
} from '../../admin-employee-schedule-manager/admin-employee-schedule.types';

interface WeeklyScheduleRow {
  dayOfWeek: number;
  dayKey: DayOfWeek;
  isActive: boolean;
  siteId: string | null;
  startTime: string;
  endTime: string;
}

@Component({
  selector: 'app-create-employee-dialog',
  imports: [CommonModule, FormsModule],
  templateUrl: './create-employee-dialog.component.html',
  styleUrl: './create-employee-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateEmployeeDialogComponent {
  readonly modelValue = input.required<boolean>();
  readonly loading = input.required<boolean>();
  readonly mostrarCredenciales = input(false);
  readonly formData = input.required<CreateEmployeeForm>();
  readonly rules = input<Record<string, unknown>>({});
  readonly credencialesRecientes = input<unknown | null>(null);
  readonly error = input<string | null>(null);
  readonly puestosTrabajo = input.required<JobPositionRequestDto[]>();
  readonly workSites = input.required<WorkSiteResponseDto[]>();

  readonly close = output<void>();
  readonly submit = output<CreateEmployeeForm>();
  readonly openJobPositions = output<void>();
  readonly copyCredentials = output<unknown>();

  readonly showScheduleSection = signal<boolean>(false);
  readonly dayKeys = DAY_KEYS;
  readonly dayLabels = DAY_LABELS;
  readonly weeklySchedules = signal<WeeklyScheduleRow[]>(this.createEmptySchedules());

  private readonly resetWeeklySchedulesEffect = effect(() => {
    if (!this.modelValue()) {
      this.weeklySchedules.set(this.createEmptySchedules());
    }
  });

  onFullNameChange(value: string): void {
    this.formData().fullName = value;
  }

  onEmployeeCodeChange(value: string): void {
    this.formData().employeeCode = value;
  }

  onEmailChange(value: string): void {
    this.formData().email = value;
  }

  onPhoneChange(value: string): void {
    this.formData().phone = value;
  }

  onWeeklyHoursChange(value: unknown): void {
    const parsed = typeof value === 'number' ? value : Number(value);
    this.formData().weeklyHours = Number.isFinite(parsed) ? parsed : null;
  }

  onPositionChange(value: string | null): void {
    this.formData().positionId = value;
  }

  toggleScheduleSection(): void {
    const nextValue = !this.showScheduleSection();
    this.showScheduleSection.set(nextValue);

    if (nextValue) {
      this.loadSchedulesFromForm();
    }
  }

  closeDialog(): void {
    this.showScheduleSection.set(false);
    this.weeklySchedules.set(this.createEmptySchedules());
    this.close.emit();
  }

  submitForm(): void {
    const schedules = this.collectActiveSchedules();
    this.syncFormSchedules();
    this.submit.emit({ ...this.formData(), schedules });
  }

  getScheduleForDay(day: DayOfWeek): WeeklyScheduleRow | null {
    return this.weeklySchedules().find((schedule) => schedule.dayKey === day) ?? null;
  }

  onScheduleActiveChanged(day: DayOfWeek, isActive: boolean): void {
    this.weeklySchedules.update((currentSchedules) =>
      currentSchedules.map((schedule) =>
        schedule.dayKey === day
          ? {
              ...schedule,
              isActive,
              siteId:
                isActive && !schedule.siteId ? (this.workSites()[0]?.id ?? null) : schedule.siteId,
            }
          : schedule,
      ),
    );

    this.syncFormSchedules();
  }

  onScheduleFieldChanged(
    day: DayOfWeek,
    field: 'siteId' | 'startTime' | 'endTime',
    value: string,
  ): void {
    this.weeklySchedules.update((currentSchedules) =>
      currentSchedules.map((schedule) =>
        schedule.dayKey === day ? { ...schedule, [field]: value } : schedule,
      ),
    );

    this.syncFormSchedules();
  }

  isScheduleComplete(schedule: WeeklyScheduleRow): boolean {
    return Boolean(schedule.siteId && schedule.startTime && schedule.endTime);
  }

  private createEmptySchedules(): WeeklyScheduleRow[] {
    return DAY_KEYS.map((dayKey, index) => ({
      dayOfWeek: index,
      dayKey,
      isActive: false,
      siteId: null,
      startTime: '',
      endTime: '',
    }));
  }

  private loadSchedulesFromForm(): void {
    const currentSchedules = this.formData().schedules ?? [];
    const weeklySchedules = this.createEmptySchedules();

    for (const currentSchedule of currentSchedules) {
      const dayIndex = weeklySchedules.findIndex(
        (schedule) => schedule.dayKey === currentSchedule.dayOfWeek,
      );

      if (dayIndex < 0) {
        continue;
      }

      weeklySchedules[dayIndex] = {
        ...weeklySchedules[dayIndex],
        isActive: true,
        siteId: currentSchedule.siteId,
        startTime: currentSchedule.startTime,
        endTime: currentSchedule.endTime,
      };
    }

    this.weeklySchedules.set(weeklySchedules);
  }

  private syncFormSchedules(): void {
    const activeSchedules = this.collectActiveSchedules();

    this.formData().schedules = activeSchedules;
  }

  private collectActiveSchedules(): NonNullable<CreateEmployeeForm['schedules']> {
    return this.weeklySchedules()
      .filter((schedule) => schedule.isActive)
      .map((schedule) => ({
        dayOfWeek: schedule.dayKey,
        startTime: schedule.startTime,
        endTime: schedule.endTime,
        siteId: schedule.siteId ?? '',
      }));
  }
}
