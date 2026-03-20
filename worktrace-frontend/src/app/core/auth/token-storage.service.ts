import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private readonly TOKEN_KEY = 'worktrace_jwt';
  private readonly ROLE_KEY = 'worktrace_role';

  constructor() { }

  public saveToken(token: string): void {
    window.localStorage.removeItem(this.TOKEN_KEY);
    window.localStorage.setItem(this.TOKEN_KEY, token);
  }

  public getToken(): string | null {
    return window.localStorage.getItem(this.TOKEN_KEY);
  }

  public saveRole(role: string): void {
    window.localStorage.removeItem(this.ROLE_KEY);
    window.localStorage.setItem(this.ROLE_KEY, role);
  }

  public getRole(): string | null {
    return window.localStorage.getItem(this.ROLE_KEY);
  }

  public clear(): void {
    window.localStorage.clear();
  }
}