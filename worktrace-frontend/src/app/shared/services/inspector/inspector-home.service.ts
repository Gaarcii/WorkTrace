import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import { InspectorHomeResponseDto } from '../../models/inspector.model';

@Injectable({
  providedIn: 'root',
})
export class InspectorHomeService {
  private readonly BASE_URL = API_CONFIG.baseUrl + 'inspector';
  private readonly http = inject(HttpClient);

  getHome(): Observable<InspectorHomeResponseDto> {
    return this.http.get<InspectorHomeResponseDto>(`${this.BASE_URL}/home`);
  }
}
