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

/**
 * @class AdminConfigService
 * @description
 * Servicio centralizado para la configuración de la aplicación por parte de un administrador.
 * Gestiona las operaciones CRUD para la información de la empresa, los centros de trabajo
 * y los tipos de incidencia, actualizando el estado local a través de signals.
 */
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

  /**
   * Obtiene los datos de la empresa desde el backend.
   *
   * @returns Un `Observable` que emite un `CompanyResponseDto` con la información de la empresa.
   */
  getCompanyData(): Observable<CompanyResponseDto> {
    return this.http.get<CompanyResponseDto>(this.COMPANY_URL);
  }

  /**
   * Actualiza los datos generales de la empresa.
   *
   * @param dto - Objeto `UpdateCompanyRequestDto` con los datos a modificar.
   * @returns Un `Observable` que emite un `UpdateCompanyResponseDto` con la respuesta del servidor.
   */
  updateCompanyData(dto: UpdateCompanyRequestDto): Observable<UpdateCompanyResponseDto> {
    return this.http.patch<UpdateCompanyResponseDto>(this.COMPANY_URL, dto);
  }

  /**
   * Actualiza el logotipo de la empresa.
   *
   * @description
   * Envía el nuevo logotipo como un archivo `multipart/form-data`.
   *
   * @param file - El archivo de imagen (`File`) del nuevo logotipo.
   * @returns Un `Observable` que emite un `UpdateCompanyLogoResponseDto` con la URL del nuevo logo.
   */
  updateCompanyLogo(file: File): Observable<UpdateCompanyLogoResponseDto> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.patch<UpdateCompanyLogoResponseDto>(`${this.COMPANY_URL}/logo`, formData);
  }

  /**
   * Obtiene la lista de todos los centros de trabajo y actualiza el `workSitesSignal`.
   *
   * @returns Un `Observable` que emite un array de `WorkSiteResponseDto`.
   */
  getWorkSites(): Observable<WorkSiteResponseDto[]> {
    return this.http
      .get<WorkSiteResponseDto[]>(this.WORK_SITES_URL)
      .pipe(tap((workSites) => this.workSitesSignal.set(workSites ?? [])));
  }

  /**
   * Crea un nuevo centro de trabajo y lo añade al `workSitesSignal` tras una creación exitosa.
   *
   * @param dto - Objeto `WorkSiteRequestDto` con los datos del nuevo centro.
   * @returns Un `Observable` que emite el `WorkSiteResponseDto` del centro recién creado.
   */
  createWorkSite(dto: WorkSiteRequestDto): Observable<WorkSiteResponseDto> {
    return this.http.post<WorkSiteResponseDto>(this.WORK_SITES_URL, dto).pipe(
      tap((created) => {
        this.workSitesSignal.update((current) => [...current, created]);
      }),
    );
  }

  /**
   * Actualiza un centro de trabajo existente y refresca su estado en `workSitesSignal`.
   *
   * @param id - El identificador del centro de trabajo a actualizar.
   * @param dto - Objeto `WorkSiteRequestDto` con los nuevos datos.
   * @returns Un `Observable` que emite el `WorkSiteResponseDto` del centro actualizado.
   */
  updateWorkSite(id: string, dto: WorkSiteRequestDto): Observable<WorkSiteResponseDto> {
    return this.http.put<WorkSiteResponseDto>(`${this.WORK_SITES_URL}/${id}`, dto).pipe(
      tap((updated) => {
        this.workSitesSignal.update((current) =>
          current.map((item) => (item.id === updated.id ? updated : item)),
        );
      }),
    );
  }

  /**
   * Elimina un centro de trabajo y lo quita de la lista en `workSitesSignal`.
   *
   * @param id - El identificador del centro de trabajo a eliminar.
   * @returns Un `Observable<void>` que se completa al finalizar la operación.
   */
  deleteWorkSite(id: string): Observable<void> {
    return this.http.delete<void>(`${this.WORK_SITES_URL}/${id}`).pipe(
      tap(() => {
        this.workSitesSignal.update((current) => current.filter((item) => item.id !== id));
      }),
    );
  }

  /**
   * Obtiene la lista de todos los tipos de incidencia y actualiza el `incidenceTypesSignal`.
   *
   * @returns Un `Observable` que emite un `IncidenceTypeResponseDto` que contiene la lista de tipos.
   */
  getIncidenceTypes(): Observable<IncidenceTypeResponseDto> {
    return this.http.get<IncidenceTypeResponseDto>(this.INCIDENCE_TYPES_URL).pipe(
      tap((response) => {
        this.incidenceTypesSignal.set(response?.types ?? []);
      }),
    );
  }

  /**
   * Crea un nuevo tipo de incidencia y lo añade al `incidenceTypesSignal`.
   *
   * @param dto - Objeto `IncidenceTypeRequestDto` con los datos del nuevo tipo.
   * @returns Un `Observable` que emite el `IncidenceTypeItemDto` del tipo recién creado.
   */
  createIncidenceType(dto: IncidenceTypeRequestDto): Observable<IncidenceTypeItemDto> {
    return this.http.post<IncidenceTypeItemDto>(this.INCIDENCE_TYPES_URL, dto).pipe(
      tap((created) => {
        this.incidenceTypesSignal.update((current) => [...current, created]);
      }),
    );
  }

  /**
   * Actualiza un tipo de incidencia existente y refresca su estado en `incidenceTypesSignal`.
   *
   * @param id - El identificador del tipo de incidencia a actualizar.
   * @param dto - Objeto `IncidenceTypeRequestDto` con los nuevos datos.
   * @returns Un `Observable` que emite el `IncidenceTypeItemDto` del tipo actualizado.
   */
  updateIncidenceType(id: string, dto: IncidenceTypeRequestDto): Observable<IncidenceTypeItemDto> {
    return this.http.put<IncidenceTypeItemDto>(`${this.INCIDENCE_TYPES_URL}/${id}`, dto).pipe(
      tap((updated) => {
        this.incidenceTypesSignal.update((current) =>
          current.map((item) => (item.id === updated.id ? updated : item)),
        );
      }),
    );
  }

  /**
   * Elimina un tipo de incidencia y lo quita de la lista en `incidenceTypesSignal`.
   *
   * @param id - El identificador del tipo de incidencia a eliminar.
   * @returns Un `Observable<void>` que se completa al finalizar la operación.
   */
  deleteIncidenceType(id: string): Observable<void> {
    return this.http.delete<void>(`${this.INCIDENCE_TYPES_URL}/${id}`).pipe(
      tap(() => {
        this.incidenceTypesSignal.update((current) => current.filter((item) => item.id !== id));
      }),
    );
  }
}
