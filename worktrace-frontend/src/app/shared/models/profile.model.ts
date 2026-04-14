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
