export interface WorkerIncidenceResponseDto {
  incidenceType: string;
  date: string;
  time: string;
  comment: string;
  status: string;
  creation: string;
}

export interface WorkerIncidenceRequestDto {
  typeId: string;
  affectedDate: string;
  time: string;
  comment: string;
}

export interface AdminIncidenceResponseDto {
  id: string;
  employeeName: string;
  jobPosition?: string;
  incidenceType: string;
  comment: string;
  status: string;
  affectedDate: string;
  createdAt: string;
  avatarUrl: string;
  adminResponse: string;
}

export type AdminIncidenceBackendStatus = 'PENDING' | 'RESOLVED' | 'REJECTED';

export interface AdminIncidenceRequestDto {
  status: AdminIncidenceBackendStatus;
  adminResponse: string;
}
