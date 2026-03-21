import { WorkScheduleResponse } from './work-schedule.model';

export interface ProfileResponse {
  nombreCompleto: string;
  avatarUrl: string;
  puestoTrabajo: string;
  telefono: string;
  tokenActualizado: string;
  horario: WorkScheduleResponse[];
}
