export interface IncidenceResponse {
  tipoIncidencia: string;
  fecha: string;
  hora: string;
  comentario: string;
  estado: string;
  creacion: string;
}

export interface IncidenceRequest {
  typeId: string;
  fechaAfectada: string;
  hora: string;
  comentario: string;
}
