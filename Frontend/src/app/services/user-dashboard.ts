import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  FinancialSummary,
  UserProfile
} from '../models/userDashboard.models';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private readonly apiUrl =
    'http://localhost:8080/api/users';

  constructor(
    private readonly http: HttpClient
  ) {}

  getProfile(
    userId: number
  ): Observable<UserProfile> {
    return this.http.get<UserProfile>(
      `${this.apiUrl}/${userId}/profile`
    );
  }

  getFinancialSummary(
    userId: number
  ): Observable<FinancialSummary> {
    return this.http.get<FinancialSummary>(
      `${this.apiUrl}/${userId}/financial-summary`
    );
  }
}