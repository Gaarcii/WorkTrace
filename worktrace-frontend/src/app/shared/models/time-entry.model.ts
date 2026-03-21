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
