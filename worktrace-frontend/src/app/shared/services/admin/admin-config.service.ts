import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  IncidenceTypeItemDto,
  IncidenceTypeProjection,
  IncidenceTypeRequestDto,
  IncidenceTypeResponseDto,
} from '../../models/incidence-type.model';
import {
  CompanyResponseDto,
  UpdateCompanyRequestDto,
  UpdateCompanyResponseDto,
  UpdateCompanyLogoResponseDto,
} from '../../models/company.model';
import { WorkSiteResponseDto } from '../../models/work-schedule.model';
import { WorkSiteRequestDto } from '../../models/work-site.model';

@Injectable({
  providedIn: 'root',
})
export class AdminConfigService {
  private readonly http = inject(HttpClient);

  private readonly COMPANY_URL = `${API_CONFIG.baseUrl}companies`;
  private readonly WORK_SITES_URL = `${API_CONFIG.baseUrl}work-sites`;
  private readonly INCIDENCE_TYPES_URL = `${API_CONFIG.baseUrl}incidence-types`;

  readonly workSitesSignal = signal<WorkSiteResponseDto[]>([]);
  readonly incidenceTypesSignal = signal<IncidenceTypeProjection[]>([]);

  getCompanyData(): Observable<CompanyResponseDto> {
    return this.http.get<CompanyResponseDto>(this.COMPANY_URL);
  }

  updateCompanyData(dto: UpdateCompanyRequestDto): Observable<UpdateCompanyResponseDto> {
    return this.http.patch<UpdateCompanyResponseDto>(this.COMPANY_URL, dto);
  }

  updateCompanyLogo(file: File): Observable<UpdateCompanyLogoResponseDto> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.patch<UpdateCompanyLogoResponseDto>(`${this.COMPANY_URL}/logo`, formData);
  }

  getWorkSites(): Observable<WorkSiteResponseDto[]> {
    return this.http
      .get<WorkSiteResponseDto[]>(this.WORK_SITES_URL)
      .pipe(tap((workSites) => this.workSitesSignal.set(workSites ?? [])));
  }

  createWorkSite(dto: WorkSiteRequestDto): Observable<WorkSiteResponseDto> {
    return this.http.post<WorkSiteResponseDto>(this.WORK_SITES_URL, dto).pipe(
      tap((created) => {
        this.workSitesSignal.update((current) => [...current, created]);
      }),
    );
  }

  updateWorkSite(id: string, dto: WorkSiteRequestDto): Observable<WorkSiteResponseDto> {
    return this.http.put<WorkSiteResponseDto>(`${this.WORK_SITES_URL}/${id}`, dto).pipe(
      tap((updated) => {
        this.workSitesSignal.update((current) =>
          current.map((item) => (item.id === updated.id ? updated : item)),
        );
      }),
    );
  }

  deleteWorkSite(id: string): Observable<void> {
    return this.http.delete<void>(`${this.WORK_SITES_URL}/${id}`).pipe(
      tap(() => {
        this.workSitesSignal.update((current) => current.filter((item) => item.id !== id));
      }),
    );
  }

  getIncidenceTypes(): Observable<IncidenceTypeResponseDto> {
    return this.http.get<IncidenceTypeResponseDto>(this.INCIDENCE_TYPES_URL).pipe(
      tap((response) => {
        this.incidenceTypesSignal.set(response?.types ?? []);
      }),
    );
  }

  createIncidenceType(dto: IncidenceTypeRequestDto): Observable<IncidenceTypeItemDto> {
    return this.http.post<IncidenceTypeItemDto>(this.INCIDENCE_TYPES_URL, dto).pipe(
      tap((created) => {
        this.incidenceTypesSignal.update((current) => [...current, created]);
      }),
    );
  }

  updateIncidenceType(id: string, dto: IncidenceTypeRequestDto): Observable<IncidenceTypeItemDto> {
    return this.http.put<IncidenceTypeItemDto>(`${this.INCIDENCE_TYPES_URL}/${id}`, dto).pipe(
      tap((updated) => {
        this.incidenceTypesSignal.update((current) =>
          current.map((item) => (item.id === updated.id ? updated : item)),
        );
      }),
    );
  }

  deleteIncidenceType(id: string): Observable<void> {
    return this.http.delete<void>(`${this.INCIDENCE_TYPES_URL}/${id}`).pipe(
      tap(() => {
        this.incidenceTypesSignal.update((current) => current.filter((item) => item.id !== id));
      }),
    );
  }
}
