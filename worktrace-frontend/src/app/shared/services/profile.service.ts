import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, Observable } from 'rxjs';
import { ProfileResponse } from '../models/profile.model';
import { API_CONFIG } from '../../core/api/api.config';

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private readonly BASE_URL = API_CONFIG.baseUrl;

  private readonly http = inject(HttpClient);

  readonly currentUser = signal<ProfileResponse | null>(null);

  fetchMyProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.BASE_URL}user/profile`).pipe(
      tap((profile) => {
        this.currentUser.set(profile);
      }),
    );
  }
}
