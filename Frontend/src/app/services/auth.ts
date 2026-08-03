import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { RegisterRequest } from '../models/register-request';
import { LoginRequest } from '../models/auth.models';
import { AuthResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root',
})
export class Auth {

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(private readonly http: HttpClient) {}

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/register`,
      data
    );
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      data
    );
  }

  saveSession(response: AuthResponse): void {
    if (response.token) {
      localStorage.setItem('token', response.token);
    }

    if (response.id != null) {
      localStorage.setItem(
        'id',
        response.id.toString()
      );
    }

    if (response.role) {
      localStorage.setItem('role', response.role);
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
    const id = localStorage.getItem('id');

    return id ? Number(id) : null;
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  isAdmin(): boolean {
    return this.getRole()?.toUpperCase() === 'ADMIN';
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('role');
    localStorage.removeItem('fullName');
  }
}