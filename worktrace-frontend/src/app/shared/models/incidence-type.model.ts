export interface IncidenceTypeProjection {
  id: string;
  name: string;
}

export interface IncidenceTypeResponseDto {
  types: IncidenceTypeProjection[];
}

export interface IncidenceTypeRequestDto {
  name: string;
}

export interface IncidenceTypeItemDto {
  id: string;
  name: string;
}
