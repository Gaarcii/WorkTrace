import { IncidenceTypeProjection } from '../../../shared/models/incidence-type.model';
import { WorkSiteResponseDto } from '../../../shared/models/work-site.model';

export type AdminConfigSectionKey = 'general' | 'sedes' | 'incidencias';

export interface AdminConfigSectionItem {
  key: AdminConfigSectionKey;
  label: string;
}

export interface CompanyFormData {
  name: string;
  cif: string;
}

export interface UiMessageState {
  show: boolean;
  message: string;
  color: 'success' | 'error';
}

export type WorkSiteView = WorkSiteResponseDto;

export type IncidenceTypeView = IncidenceTypeProjection;
