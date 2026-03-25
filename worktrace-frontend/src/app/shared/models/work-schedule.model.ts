export interface WorkScheduleResponse {
  lugar: string;
  ubicacion: string;
  diaSemana: string;
  start: string;
  end: string;
  horas: number;
}

export interface DiaHorario {
  id: string;
  inicial: string;
  trabaja: boolean;
  start: string | null;
  end: string | null;
  lugar: string;
  ubicacion: string;
}
