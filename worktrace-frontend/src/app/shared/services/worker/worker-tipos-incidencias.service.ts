import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  IncidenceTypeProjection,
  IncidenceTypeResponseDto,
} from '../../models/incidence-type.model';
@Injectable({
  providedIn: 'root',
})
export class WorkerIncidenceTypesService {
  private readonly http = inject(HttpClient);

  private readonly BASE_URL = API_CONFIG.baseUrl + 'incidence/types';

  readonly tiposSignal = signal<IncidenceTypeProjection[]>([]);

  obtenerTipos(): Observable<IncidenceTypeResponseDto> {
    return this.http.get<IncidenceTypeResponseDto>(this.BASE_URL).pipe(
      tap((response) => {
        if (response && response.tipos) {
          this.tiposSignal.set(response.tipos);
        }
      }),
    );
  }
}
