export interface AdminIncidenciaView {
  id: string;
  status: string;
  created_at: string;
  comment: string;
  description?: string;
  admin_response?: string | null;
  profiles?: {
    full_name?: string;
    avatar_url?: string | null;
    job_positions?: {
      title?: string;
    } | null;
  };
  incidence_types?: {
    name?: string;
  };
}

export type AdminGestionEstado = 'Resuelta' | 'Rechazada';
