import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { InspectorHomeResponseDto } from '../../models/inspector.model';

@Injectable({
  providedIn: 'root',
})
export class InspectorHomeService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly BASE_URL_EXPORTAR = API_CONFIG.baseUrl;
  private readonly http = inject(HttpClient);

  getHome(): Observable<InspectorHomeResponseDto> {
    return this.http.get<InspectorHomeResponseDto>(`${this.BASE_URL}/home`);
  }

  exportReportPdf(startDate: Date | string, endDate: Date | string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', this.toIsoDate(startDate))
      .set('endDate', this.toIsoDate(endDate));

    return this.http.get(`${this.BASE_URL_EXPORTAR}time-entries/admin/export/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  exportarInformeEmpresaExcel(
    fechaInicio: Date | string,
    fechaFin: Date | string,
  ): Observable<Blob> {
    return this.exportReportExcel(fechaInicio, fechaFin);
  }

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
