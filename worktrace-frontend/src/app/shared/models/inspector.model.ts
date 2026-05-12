import { WorkScheduleResponse } from './work-schedule.model';

export interface InspectorHomeResponseDto {
  totalEmployees: number;
  totalIncidences: number;
  activeEmployeesToday: number;
  totalAuditLogs: number;
}

export interface EmployeeDto {
  id: string;
  photo: string;
  name: string;
  jobPosition: string;
  email: string;
  phone: string;
  employeeCode: string;
  role: string;
}

export interface EmployeeDetailDto {
  id: string;
  registrationDate: string;
  weeklyHours: number;
  isActive: boolean;
  workSchedules: WorkScheduleResponse[];
}
export interface InspectorIncidenceDto {
  name: string;
  email: string;
  image: string;
  incidenceType: string;
  dateTime: string;
  status: string;
  comment: string;
  resolvedBy: string;
}

export interface InspectorDailyClosureDto {
  date: string;
  status: string;
  timeEntriesCount: number;
  dayHash: string;
  previousDayHash: string;
  computedAt: string;
}

export interface InspectorAuditDto {
  id: string;
  dateTime: string;
  action: string;
  justification: string;
  actor: string;
}
export interface InspectorAuditDetailDto {
  id: string;
  timestamp: string;
  reason: string;
  actorName: string;
  previousData: string;
}

export interface InspectorRequestDto {
  email: string;
  fullName: string;
  phone: string;
}
