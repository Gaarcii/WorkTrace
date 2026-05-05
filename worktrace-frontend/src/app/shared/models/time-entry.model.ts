import { WorkerIncidenceResponseDto } from './incidence.model';

export interface DailySummaryResponse {
  minutosAcumulados: number;
  minutosObjetivo: number;
  horaEntrada: string;
  ultimosFichajes: UltimosFichajes[];
}

/**
 * @deprecated Use DailySummaryResponse instead
 */
export type ResumenResponse = DailySummaryResponse;

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

export interface HistoryResponse {
  minutosTrabajadosSemana: string;
  minutosObjetivoSemana: string;
  registrosDia: UltimosFichajes[];
  minutosTrabajadosDia: number;
  minutosObjetivoDia: number;
}

/**
 * @deprecated Use HistoryResponse instead
 */
export type HistorialResponse = HistoryResponse;

export interface DailyStatisticResponse {
  fecha: string;
  minutosTrabajados: number;
  minutosPrevistos: number;
}

/**
 * @deprecated Use DailyStatisticResponse instead
 */
export type EstadisticaDiariaResponse = DailyStatisticResponse;

export interface StatisticsResponse {
  minutosTrabajadosTotal: number;
  balanceMinutos: number;
  jornadasIncompletas: number;
  incidencias: number;
  resumenDiario: DailyStatisticResponse[];
  incidenciasList: WorkerIncidenceResponseDto[];
}

/**
 * @deprecated Use StatisticsResponse instead
 */
export type EstadisticasResponse = StatisticsResponse;

export interface ActiveWorkerDto {
  trabajadorId: string;
  nombreCompleto: string;
  puestoTrabajo: string;
  urlAvatar: string;
  horaFichaje: string;
  puntualidad: number;
}

export interface TotalHoursTodayResponseDto {
  minutosTotales: number;
}

/**
 * @deprecated Use TotalHoursTodayResponseDto instead
 */
export type HorasTrabajadasHoyResponseDto = TotalHoursTodayResponseDto;

export interface DailyTimeEntryCountDto {
  fecha: string;
  numFichajes: number;
}

/**
 * @deprecated Use DailyTimeEntryCountDto instead
 */
export type DailyFichajeCountDto = DailyTimeEntryCountDto;

export interface AdminTimeEntryByDateResponseDto {
  id: string;
  trabajadorId: string;
  nombreTrabajador: string;
  puestoTrabajo: string | null;
  avatarUrl: string | null;
  fecha: string;
  entrada: string;
  salida: string | null;
  minutosTrabajados: number | null;
}

/**
 * @deprecated Use AdminTimeEntryByDateResponseDto instead
 */
export type AdminFichajeDiaDto = AdminTimeEntryByDateResponseDto;

export interface TimeEntryTableResponseDto {
  id: string;
  fecha: string;
  entrada: string;
  salida: string | null;
  latEntrada: number | null;
  lngEntrada: number | null;
  latSalida: number | null;
  lngSalida: number | null;
  horasTrabajadas: number | null;
  deletedAt?: string | null;
  work_date?: string;
  start_at?: string;
  end_at?: string | null;
  start_lat?: number | null;
  start_lng?: number | null;
  end_lat?: number | null;
  end_lng?: number | null;
  deleted_at?: string | null;
}

/**
 * @deprecated Use TimeEntryTableResponseDto instead
 */
export type FichajeTablaResponseDto = TimeEntryTableResponseDto;

export interface EditTimeEntryRequestDto {
  entrada: string;
  salida: string | null;
  justificacion: string;
}

export interface VoidTimeEntryRequestDto {
  justificacion: string;
}

/**
 * @deprecated Use VoidTimeEntryRequestDto instead
 */
export type AnularTimeEntryRequestDto = VoidTimeEntryRequestDto;
