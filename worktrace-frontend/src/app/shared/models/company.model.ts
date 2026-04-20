export interface UpdateCompanyRequestDto {
  companyName: string;
  cif: string;
}

export interface CompanyResponseDto {
  companyName: string;
  cif: string;
  logoUrl: string | null;
}

export interface UpdateCompanyResponseDto {
  message: string;
}

export interface UpdateCompanyLogoResponseDto {
  message: string;
  logoUrl: string;
}
