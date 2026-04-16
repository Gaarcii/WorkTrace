import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import {
  EditableDaySchedule,
  ScheduleTemplate,
  WorkSiteResponseDto,
} from '../../admin-employee-schedule.types';
import { ScheduleDayRowComponent } from '../schedule-day-row/schedule-day-row.component';
import { ScheduleQuickActionsComponent } from '../schedule-quick-actions/schedule-quick-actions.component';

@Component({
  selector: 'app-admin-employee-schedule-content',
  imports: [ScheduleQuickActionsComponent, ScheduleDayRowComponent],
  templateUrl: './admin-employee-schedule-content.component.html',
  styleUrl: './admin-employee-schedule-content.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminEmployeeScheduleContentComponent {
  public readonly loading = input.required<boolean>();
  public readonly error = input<string | null>(null);
  public readonly schedules = input.required<EditableDaySchedule[]>();
  public readonly workSites = input.required<WorkSiteResponseDto[]>();

  public readonly activateAll = output<void>();
  public readonly deactivateAll = output<void>();
  public readonly applyToWorkweek = output<number>();
  public readonly applyTemplate = output<ScheduleTemplate>();
  public readonly updateSchedule = output<EditableDaySchedule>();
  public readonly copyToAll = output<EditableDaySchedule>();

  public onActivateAll(): void {
    this.activateAll.emit();
  }

  public onDeactivateAll(): void {
    this.deactivateAll.emit();
  }

  public onApplyToWorkweek(day: number): void {
    this.applyToWorkweek.emit(day);
  }

  public onApplyTemplate(template: ScheduleTemplate): void {
    this.applyTemplate.emit(template);
  }

  public onUpdateSchedule(schedule: EditableDaySchedule): void {
    this.updateSchedule.emit(schedule);
  }

  public onCopyToAll(schedule: EditableDaySchedule): void {
    this.copyToAll.emit(schedule);
  }
}
