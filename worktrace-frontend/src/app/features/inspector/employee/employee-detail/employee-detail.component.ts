import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { EmployeeDto, EmployeeDetailDto } from '../../../../shared/models/inspector.model';

@Component({
  selector: 'app-employee-detail',
  imports: [
    CommonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatButtonModule,
  ],
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeDetailComponent {
  readonly employee = input.required<EmployeeDto>();
  readonly detail = input<EmployeeDetailDto | null>(null);
  readonly loadingSchedule = input<boolean>(false);

  readonly close = output<void>();

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  formatTime(timeString?: string): string {
    if (!timeString) return 'N/A';
    return timeString.substring(0, 5);
  }

  calculateHours(startTime?: string, endTime?: string): string {
    if (!startTime || !endTime) return 'N/A';
    const [startHour, startMin] = startTime.split(':').map(Number);
    const [endHour, endMin] = endTime.split(':').map(Number);
    const startMinutes = startHour * 60 + startMin;
    const endMinutes = endHour * 60 + endMin;
    let diffMinutes = endMinutes - startMinutes;
    if (diffMinutes < 0) diffMinutes += 24 * 60;
    return `${Math.floor(diffMinutes / 60)}h`;
  }

  getDayName(dayOfWeek: string): string {
    const days: Record<string, string> = {
      MONDAY: 'Lunes',
      TUESDAY: 'Martes',
      WEDNESDAY: 'Miércoles',
      THURSDAY: 'Jueves',
      FRIDAY: 'Viernes',
      SATURDAY: 'Sábado',
      SUNDAY: 'Domingo',
    };
    return days[dayOfWeek?.toUpperCase()] || dayOfWeek || 'N/A';
  }

  getDayIcon(dayOfWeek: string): string {
    const day = dayOfWeek?.toUpperCase();
    return day === 'SATURDAY' || day === 'SUNDAY' ? 'calendar_month' : 'work';
  }
}
