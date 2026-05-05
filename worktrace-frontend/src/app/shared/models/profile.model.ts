import { WorkScheduleResponse } from './work-schedule.model';

export interface ProfileResponse {
  nombreCompleto: string;
  avatarUrl: string;
  puestoTrabajo: string;
  email: string;
  telefono: string;
  tokenActualizado: string;
  horario: WorkScheduleResponse[];
}

export interface ProfileRequest {
  avatar?: File | null;
  email: string;
  telefono: string;
  contrasenaActual: string;
  eliminarAvatar: string;
}

export interface DepartmentStatDto {
  departamento: string;
  totalTrabajadores: number;
  trabajadoresActivos: number;
}

export interface EmployeeResponseDto {
  id: string;
  nombre: string;
  dni: string;
  email: string;
  avatarUrl: string;
  telefono: string;
  puesto: string;
  horasSemanales: string;
  estado: string;
  fechaAlta: string;
  positionId?: string | null;
  horario?: WorkScheduleResponse[];
}

export interface JobPositionResponseDto {
  id: string;
  nombre: string;
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
  schedules?: Array<{
    dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
    startTime: string;
    endTime: string;
    siteId: string;
  }>;
}

export interface SpringPageResponse<T> {
  content: T[];
  totalElements?: number;
}
