export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface WorkScheduleResponse {
  place: string;
  location: string;
  dayOfWeek: DayOfWeek;
  start: string;
  end: string;
  hours: number;
}

export interface WorkScheduleRequest {
  employeeId: string;
  schedules: WorkScheduleDayRequest[];
}

export interface WorkScheduleDayRequest {
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  siteId: string;
}

export interface WorkSiteResponseDto {
  id: string;
  name: string;
  address: string;
}

export interface ScheduleDay {
  id: string;
  initial: string;
  work: boolean;
  start: string | null;
  end: string | null;
  place: string;
  location: string;
}
