import { EmployeeResponseDto } from '../../../../shared/models/profile.model';
import {
  DayOfWeek,
  WorkScheduleResponse,
  WorkSiteResponseDto,
} from '../../../../shared/models/work-schedule.model';

export interface EmployeeScheduleResponseDto extends EmployeeResponseDto {
  schedule?: WorkScheduleResponse[];
}

export interface EditableDaySchedule {
  dayOfWeek: number;
  dayKey: DayOfWeek;
  isActive: boolean;
  siteId: string | null;
  startTime: string;
  endTime: string;
}

export type ScheduleTemplate = 'morning' | 'full';

export interface DayOption {
  value: number;
  label: string;
}

export interface TemplateDefinition {
  startTime: string;
  endTime: string;
}

export const DAY_KEYS: readonly DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
] as const;

export const DAY_LABELS: readonly string[] = [
  'Lunes',
  'Martes',
  'Miercoles',
  'Jueves',
  'Viernes',
  'Sabado',
  'Domingo',
] as const;

export type { WorkSiteResponseDto };
