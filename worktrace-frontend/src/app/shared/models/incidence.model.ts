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

export interface AdminIncidenceResponseDto {
  id: string;
  nombreTrabajador: string;
  puestoTrabajo?: string;
  tipoIncidencia: string;
  comentario: string;
  estado: string;
  fechaAfectada: string;
  creacion: string;
  avatarUrl: string;
  adminResponse: string;
}

export type AdminIncidenceBackendStatus = 'PENDING' | 'RESOLVED' | 'REJECTED';

export interface AdminIncidenceManageRequestDto {
  estado: AdminIncidenceBackendStatus;
  respuestaAdmin: string;
}
