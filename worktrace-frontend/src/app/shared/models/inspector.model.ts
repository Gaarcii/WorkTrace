import { WorkScheduleResponse } from './work-schedule.model';

export interface InspectorHomeResponseDto {
  totalEmpleados: number;
  totalIncidencias: number;
  trabajadoresActivosHoy: number;
  totalAuditLogs: number;
}

export interface EmpleadoDto {
  id: string;
  photo: string;
  name: string;
  jobPosition: string;
  email: string;
  phone: string;
  employeeCode: string;
  role: string;
}

export interface EmpleadoDetalleDto {
  id: string;
  registrationDate: string;
  weeklyHours: number;
  isActive: boolean;
  workSchedules: WorkScheduleResponse[];
}
export interface InspectorIncidenceDto {
  nombre: string;
  correo: string;
  imagen: string;
  tipoIncidencia: string;
  fechaHora: string;
  estado: string;
  comentario: string;
  resueltaPor: string;
}
