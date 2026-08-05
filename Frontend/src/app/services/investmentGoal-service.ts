import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  InvestmentGoalRequest,
  InvestmentGoalResponse
} from '../models/investmentGoal-models';

@Injectable({
  providedIn: 'root'
})
export class investmentGoalService {

  private readonly apiUrl =
    'http://localhost:8080/api/financial-goals';

  constructor(
    private readonly http: HttpClient
  ) {}

  create(
    request: InvestmentGoalRequest
  ): Observable<InvestmentGoalResponse> {
    return this.http.post<InvestmentGoalResponse>(
      this.apiUrl,
      request
    );
  }

  getById(
    goalId: number
  ): Observable<InvestmentGoalResponse> {
    return this.http.get<InvestmentGoalResponse>(
      `${this.apiUrl}/${goalId}`
    );
  }

  getByUserId(
    userId: number
  ): Observable<InvestmentGoalResponse[]> {
    const params = new HttpParams()
      .set('userId', userId.toString());

    return this.http.get<InvestmentGoalResponse[]>(
      this.apiUrl,
      { params }
    );
  }

  update(
    goalId: number,
    request: InvestmentGoalRequest
  ): Observable<InvestmentGoalResponse> {
    return this.http.put<InvestmentGoalResponse>(
      `${this.apiUrl}/${goalId}`,
      request
    );
  }

  contribute(
    goalId: number,
    amount: number
  ): Observable<InvestmentGoalResponse> {
    return this.http.post<InvestmentGoalResponse>(
      `${this.apiUrl}/${goalId}/contribute`,
      { amount }
    );
  }

  delete(
    goalId: number
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${goalId}`
    );
  }
}