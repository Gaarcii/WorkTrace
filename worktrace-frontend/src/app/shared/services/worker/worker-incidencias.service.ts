import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../../core/api/api.config';
import {
  WorkerIncidenceRequestDto,
  WorkerIncidenceResponseDto,
} from '../../models/incidence.model';

@Injectable({
  providedIn: 'root',
})
export class WorkerIncidenciasService {
  private readonly http = inject(HttpClient);

  private readonly BASE_URL = API_CONFIG.baseUrl + 'incidences';

  readonly incidenciasSignal = signal<WorkerIncidenceResponseDto[]>([]);

  obtenerMisIncidencias(): Observable<WorkerIncidenceResponseDto[]> {
    return this.http
      .get<WorkerIncidenceResponseDto[]>(this.BASE_URL)
      .pipe(tap((incidencias) => this.incidenciasSignal.set(incidencias)));
  }

  crearIncidencia(request: WorkerIncidenceRequestDto): Observable<WorkerIncidenceResponseDto> {
    return this.http.post<WorkerIncidenceResponseDto>(this.BASE_URL, request).pipe(
      tap((nuevaIncidencia) => {
        this.incidenciasSignal.update((actuales) => [nuevaIncidencia, ...actuales]);
      }),
    );
  }
}
