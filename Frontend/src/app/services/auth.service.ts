import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  LoginRequest,
  AuthResponse
} from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl =
    'http://localhost:8080/api/auth';

  private readonly platformId = inject(PLATFORM_ID);

  constructor(
    private readonly http: HttpClient
  ) {}

  // localStorage doesn't exist during server-side prerendering/SSR.
  private get isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      request
    );
  }

  saveSession(response: AuthResponse): void {
  if (!this.isBrowser) {
    return;
  }

  localStorage.setItem('token', response.token);
  localStorage.setItem('role', response.role);
  localStorage.setItem('email', response.email);

  if (response.id != null) {
    localStorage.setItem(
      'id',
      response.id.toString()
    );
  }

  if (response.fullName) {
    localStorage.setItem(
      'fullName',
      response.fullName
    );
  }
}

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem('token') : null;
  }

  getUserId(): number | null {
    const userId = this.isBrowser ? localStorage.getItem('id') : null;

    return userId
      ? Number(userId)
      : null;
  }

  getRole(): string | null {
    return this.isBrowser ? localStorage.getItem('role') : null;
  }

  isLoggedIn(): boolean {
    return Boolean(this.getToken());
  }

  isAdmin(): boolean {
    return this.getRole()?.toUpperCase() === 'ADMIN';
  }

  logout(): void {
    if (!this.isBrowser) {
      return;
    }

    localStorage.removeItem('token');
    localStorage.removeItem('id');
    localStorage.removeItem('role');
    localStorage.removeItem('fullName');
  }
}