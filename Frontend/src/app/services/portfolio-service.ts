import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  InvestmentResponse
} from '../models/portfolio-models';

@Injectable({
  providedIn: 'root'
})
export class PortfolioService {

  private readonly apiUrl =
    'http://localhost:8080/api';

  constructor(
    private readonly http: HttpClient
  ) {}

  getInvestmentsByUserId(
    userId: number
  ): Observable<InvestmentResponse[]> {
    return this.http.get<InvestmentResponse[]>(
      `${this.apiUrl}/investments/user/${userId}`
    );
  }

  completeCurrentMonth(
    userId: number,
    planId: number
  ): Observable<void> {
    const params = new HttpParams()
      .set('userId', String(userId))
      .set('planId', String(planId));

    return this.http.post<void>(
      `${this.apiUrl}/investments/complete-month`,
      null,
      { params }
    );
  }
}