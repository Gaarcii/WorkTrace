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

export interface InspectorDailyClosureDto {
  fecha: string;
  estado: string;
  numeroFichajes: number;
  hashDelDia: string;
  hashDiaAnterior: string;
  computado: string;
}

export interface InspectorAuditDto {
  id: string;
  fechaHora: string;
  accion: string;
  justificacion: string;
  actor: string;
}
export interface InspectorAuditDetailDto {
  id: string;
  fechaEliminacion: string;
  motivo: string;
  ejecutador: string;
  datosAntesModificacion: string;
}

export interface InspectorRequestDto {
  email: string;
  fullName: string;
  phone: string;
}
