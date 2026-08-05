import { Injectable } from '@angular/core';
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

  constructor(
    private readonly http: HttpClient
  ) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      request
    );
  }

  saveSession(response: AuthResponse): void {
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
    return localStorage.getItem('token');
  }

  getUserId(): number | null {
    const userId = localStorage.getItem('id');

    return userId
      ? Number(userId)
      : null;
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  isLoggedIn(): boolean {
    return Boolean(this.getToken());
  }

  isAdmin(): boolean {
    return this.getRole()?.toUpperCase() === 'ADMIN';
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('id');
    localStorage.removeItem('role');
    localStorage.removeItem('fullName');
  }
}