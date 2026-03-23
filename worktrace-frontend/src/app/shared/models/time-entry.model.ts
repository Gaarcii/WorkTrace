import { IncidenceResponse } from './incidence.model';

export interface ResumenResponse {
  minutosAcumulados: number;
  minutosObjetivo: number;
  horaEntrada: string;
  ultimosFichajes: UltimosFichajes[];
}

export interface TimeEntryResponse {
  id: string;
  starAt: string;
  endAt: string;
  status: string;
}

export interface TimeEntryRequest {
  lat: number;
  lng: number;
  accuracyMeters: number;
}

export interface UltimosFichajes {
  timeEntryId: string;
  tipoEvento: string;
  fecha: string;
}

export interface HistorialResponse {
  minutosTrabajadosSemana: string;
  minutosObjetivoSemana: string;
  registrosDia: UltimosFichajes[];
  minutosTrabajadosDia: number;
  minutosObjetivoDia: number;
}

export interface EstadisticaDiariaResponse {
  fecha: string;
  minutosTrabajados: number;
  minutosPrevistos: number;
}

export interface EstadisticasResponse {
  minutosTrabajadosTotal: number;
  balanceMinutos: number;
  jornadasIncompletas: number;
  incidencias: number;
  resumenDiario: EstadisticaDiariaResponse[];
  incidenciasList: IncidenceResponse[];
}
