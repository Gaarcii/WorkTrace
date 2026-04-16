export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface WorkScheduleResponse {
  lugar: string;
  ubicacion: string;
  diaSemana: DayOfWeek;
  start: string;
  end: string;
  horas: number;
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

export interface DiaHorario {
  id: string;
  inicial: string;
  trabaja: boolean;
  start: string | null;
  end: string | null;
  lugar: string;
  ubicacion: string;
}
