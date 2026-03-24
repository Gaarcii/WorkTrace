export interface IncidenceTypeProjection {
  id: string;
  name: string;
}

export interface IncidenceTypeResponseDto {
  tipos: IncidenceTypeProjection[];
}
