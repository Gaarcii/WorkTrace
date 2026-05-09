import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { InspectorHomeResponseDto } from '../../models/inspector.model';

/**
 * @class InspectorHomeService
 * @description
 * Servicio para la página principal del rol Inspector. Proporciona los datos
 * agregados necesarios para el dashboard y la funcionalidad para exportar
 * informes generales de la empresa.
 */
@Injectable({
  providedIn: 'root',
})
export class InspectorHomeService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly BASE_URL_EXPORTAR = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);

  /**
   * Obtiene los datos y estadísticas principales para el dashboard del inspector.
   *
   * @returns Un `Observable` que emite un objeto `InspectorHomeResponseDto` con los datos agregados.
   */
  getHome(): Observable<InspectorHomeResponseDto> {
    return this.http.get<InspectorHomeResponseDto>(`${this.BASE_URL}/home`);
  }

  /**
   * Genera y descarga un informe completo de fichajes de la empresa en formato PDF.
   *
   * @param startDate - La fecha de inicio del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @param endDate - La fecha de fin del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un `Blob` con el contenido del archivo PDF.
   */
  exportReportPdf(startDate: Date | string, endDate: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http.get(`${this.BASE_URL_EXPORTAR}time-entries/admin/export/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Genera y descarga un informe completo de fichajes de la empresa en formato Excel.
   *
   * @param startDate - La fecha de inicio del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @param endDate - La fecha de fin del informe (objeto `Date` o string 'YYYY-MM-DD').
   * @returns Un `Observable` que emite un `Blob` con el contenido del archivo Excel.
   */
  exportReportExcel(startDate: Date | string, endDate: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http.get(`${this.BASE_URL_EXPORTAR}time-entries/admin/export/excel`, {
      params,
      responseType: 'blob',
    });
  }

  private toIsoDate(value: Date | string): string {
    if (typeof value === 'string') {
      return value;
    }

    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
