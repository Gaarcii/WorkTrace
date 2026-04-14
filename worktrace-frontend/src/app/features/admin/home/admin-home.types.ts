export interface DashboardAlert {
  id: string;
  type: string;
  comment: string;
  description: string;
  createdAt: string;
  workerName: string;
  jobTitle: string | null;
}

export interface WeekChartDay {
  dia: string;
  valor: number;
  porcentaje: number;
  activo: boolean;
  date: string;
  workDate: string;
  seleccionado: boolean;
}

export interface DepartmentSummary {
  nombre: string;
  activos: number;
  total: number;
  porcentaje: number;
  horas: number | null;
  activo: boolean;
}

export interface ActiveWorkerCard {
  id: string;
  nombre: string;
  departamento: string;
  avatar: string | null;
  iniciales: string;
  estado: string;
  entrada: string;
  retraso: boolean;
  minutosRetraso: number;
  fechaIso: string | null;
}

export interface SelectedDayEntry {
  id: string;
  nombre: string;
  departamento: string;
  avatar: string | null;
  iniciales: string;
  entrada: string;
  salida: string | null;
  duracion: string | null;
}

export interface QuickAction {
  icono: string;
  texto: string;
}

export type FormatoExportacion = 'pdf' | 'excel';
export type RangoExportacion = 'hoy' | 'mes' | 'anio' | 'todo';
