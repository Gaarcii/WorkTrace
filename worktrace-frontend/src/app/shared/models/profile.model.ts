import { WorkScheduleResponse } from './work-schedule.model';

export interface ProfileResponse {
  fullName: string;
  avatarUrl: string;
  jobPosition: string;
  email: string;
  phone: string;
  updatedToken: string;
  schedule: WorkScheduleResponse[];
}

export interface ProfileRequest {
  avatar?: File | null;
  email: string;
  phone: string;
  actualPassword: string;
  deleteAvatar: string;
}

export interface DepartmentStatDto {
  department: string;
  totalWorkers: number;
  activeWorkers: number;
}

export interface EmployeeResponseDto {
  id: string;
  name: string;
  dni: string;
  email: string;
  avatarUrl: string;
  phone: string;
  jobPosition: string;
  weeklyHours: string;
  status: string;
  registrationDate: string;
  positionId?: string | null;
  schedule?: WorkScheduleResponse[];
}

export interface JobPositionResponseDto {
  id: string;
  name: string;
}

export interface JobPositionRequestDto {
  id: string;
  title: string;
}

export interface EditEmployeeWorkDataRequestDto {
  positionId: string;
  weeklyHours: number;
}

export interface CreateEmployeeProfileRequestDto {
  fullName: string;
  employeeCode: string;
  phone: string;
  positionId: string;
  weeklyHours?: number;
}

export interface CreateEmployeeRequestDto {
  email: string;
  profile: CreateEmployeeProfileRequestDto;
  schedules?: {
    dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
    startTime: string;
    endTime: string;
    siteId: string;
  }[];
}

export interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}
